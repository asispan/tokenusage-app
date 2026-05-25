package com.llmusage.monitor.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.data.prefs.ThemeMode
import com.llmusage.monitor.data.security.KeystoreEncryption
import com.llmusage.monitor.domain.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SettingsUiState(
    val currency: String = "USD",
    val refreshIntervalMinutes: Int = 60,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val lastExportPath: String? = null
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        container.settings.currency,
        container.settings.refreshIntervalMinutes,
        container.settings.themeMode,
        container.settings.notificationsEnabled
    ) { currency, interval, mode, notif ->
        SettingsUiState(currency, interval, mode, notif)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setCurrency(value: String) { viewModelScope.launch { container.settings.setCurrency(value) } }
    fun setRefreshInterval(min: Int) {
        viewModelScope.launch {
            container.settings.setRefreshInterval(min)
            com.llmusage.monitor.worker.UsageRefreshWorker.schedule(
                context = applicationContext(),
                intervalMinutes = min
            )
        }
    }
    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { container.settings.setThemeMode(mode) } }
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { container.settings.setNotificationsEnabled(enabled) }
    }

    /** Wipes Room + DataStore + Keystore. Irreversible. */
    fun resetAllData() {
        viewModelScope.launch {
            container.db.clearAllTables()
            container.settings.clearAll()
            KeystoreEncryption.deleteKey()
        }
    }

    fun exportCsv(context: Context, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                val rows = container.usageRepository.exportAll()
                val outDir = File(context.cacheDir, "exports").also { it.mkdirs() }
                val file = File(outDir, "llm_usage_${System.currentTimeMillis()}.csv")
                file.printWriter(Charsets.UTF_8).use { w ->
                    w.println("date_epoch_day,provider_id,input_tokens,output_tokens,total_tokens,estimated_cost_usd,limit_usd,limit_tokens")
                    rows.forEach { r ->
                        w.println(
                            listOf(
                                r.dateEpochDay,
                                r.providerId,
                                r.inputTokens,
                                r.outputTokens,
                                r.totalTokens,
                                r.estimatedCostUsd,
                                r.limitUsd ?: "",
                                r.limitTokens ?: ""
                            ).joinToString(",")
                        )
                    }
                }
                file.absolutePath
            }
            onDone(path)
        }
    }

    private fun applicationContext(): Context {
        // Best-effort accessor; in v1 we rely on the worker schedule call site
        // to pass real context. This avoids leaking a Context into the VM.
        return container.notifications.context
    }
}
