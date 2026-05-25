package com.llmusage.monitor

import android.content.Context
import com.llmusage.monitor.data.db.AppDatabase
import com.llmusage.monitor.data.prefs.SettingsDataStore
import com.llmusage.monitor.data.repository.AlertRepository
import com.llmusage.monitor.data.repository.PricingRepository
import com.llmusage.monitor.data.repository.ProviderRepository
import com.llmusage.monitor.data.repository.UsageRepository
import com.llmusage.monitor.notification.NotificationHelper
import com.llmusage.monitor.providers.ProviderRegistry
import com.llmusage.monitor.worker.UsageSyncManager

/**
 * Hand-rolled dependency container — we avoid Hilt to keep the build small
 * and predictable. Held as a singleton on [LLMUsageApp].
 */
class AppContainer(private val context: Context) {

    val db: AppDatabase by lazy { AppDatabase.get(context) }
    val settings: SettingsDataStore by lazy { SettingsDataStore(context) }

    val providerRepository: ProviderRepository by lazy { ProviderRepository(db.providerConfigDao()) }
    val usageRepository: UsageRepository by lazy { UsageRepository(db.usageSnapshotDao()) }
    val pricingRepository: PricingRepository by lazy { PricingRepository(db.pricingDao()) }
    val alertRepository: AlertRepository by lazy { AlertRepository(db.alertDao()) }

    val providers: ProviderRegistry by lazy { ProviderRegistry(pricingRepository) }
    val notifications: NotificationHelper by lazy { NotificationHelper(context) }

    val syncManager: UsageSyncManager by lazy {
        UsageSyncManager(
            context = context,
            providers = providers,
            providerRepository = providerRepository,
            usageRepository = usageRepository,
            alertRepository = alertRepository,
            notifications = notifications
        )
    }
}
