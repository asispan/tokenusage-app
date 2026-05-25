package com.llmusage.monitor.worker

import android.content.Context
import com.llmusage.monitor.data.repository.AlertRepository
import com.llmusage.monitor.data.repository.ProviderRepository
import com.llmusage.monitor.data.repository.UsageRepository
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.notification.NotificationHelper
import com.llmusage.monitor.providers.ProviderRegistry
import com.llmusage.monitor.providers.ProviderResult
import com.llmusage.monitor.util.TimeUtils
import com.llmusage.monitor.widget.UsageWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.flow.first

/**
 * Pulls usage from every configured provider, writes snapshots, fires
 * threshold notifications, and asks the home-screen widget to redraw.
 */
class UsageSyncManager(
    private val context: Context,
    private val providers: ProviderRegistry,
    private val providerRepository: ProviderRepository,
    private val usageRepository: UsageRepository,
    private val alertRepository: AlertRepository,
    private val notifications: NotificationHelper
) {

    suspend fun syncAllOnce() {
        val configs = providerRepository.observeAll().first()
        configs.forEach { cfg ->
            if (!cfg.enabled) return@forEach
            val provider = providers.byId(cfg.providerId)
            val apiKey = providerRepository.decryptedKey(cfg.providerId)
            if (!provider.isConfigured(apiKey, cfg.extrasJson)) {
                providerRepository.recordSync(cfg.providerId, success = false, error = "Not configured")
                return@forEach
            }

            when (val r = provider.fetchUsage(apiKey, cfg.extrasJson)) {
                is ProviderResult.Success -> {
                    usageRepository.upsertSnapshot(cfg.providerId, TimeUtils.todayEpochDay(), r.data)
                    providerRepository.recordSync(cfg.providerId, success = true)
                    maybeFireAlerts(cfg.providerId, r.data)
                }
                is ProviderResult.Failure -> {
                    providerRepository.recordSync(cfg.providerId, success = false, error = r.message)
                }
                is ProviderResult.Unsupported -> {
                    // Don't overwrite manual entries; just clear last error.
                    providerRepository.recordSync(cfg.providerId, success = true, error = null)
                }
            }
        }

        // Redraw widget. Failure is non-fatal; the widget will retry on its own schedule.
        runCatching {
            val mgr = GlanceAppWidgetManager(context)
            val ids = mgr.getGlanceIds(UsageWidget::class.java)
            ids.forEach { UsageWidget().update(context, it) }
        }
    }

    private suspend fun maybeFireAlerts(providerId: String, usage: UsageData) {
        val percent = usage.quotaPercent ?: return
        val today = TimeUtils.todayEpochDay()
        val alerts = alertRepository.activeFor(providerId)
        alerts.forEach { alert ->
            val crossed = (percent * 100).toInt() >= alert.thresholdPercent
            val alreadyFiredToday = alert.lastFiredEpochDay == today
            if (crossed && !alreadyFiredToday) {
                notifications.notifyThreshold(
                    providerLabel = com.llmusage.monitor.domain.model.ProviderType.fromId(providerId).displayName,
                    percent = alert.thresholdPercent,
                    notifId = (providerId.hashCode() xor alert.thresholdPercent)
                )
                alertRepository.update(alert.copy(lastFiredEpochDay = today))
            }
        }
    }
}
