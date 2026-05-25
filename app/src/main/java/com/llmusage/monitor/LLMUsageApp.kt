package com.llmusage.monitor

import android.app.Application
import androidx.work.Configuration
import com.llmusage.monitor.worker.UsageRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LLMUsageApp : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Seed defaults and notification channels off the main thread.
        appScope.launch {
            container.providerRepository.ensureSeeded()
            container.pricingRepository.ensureDefaultsSeeded()
        }
        container.notifications.ensureChannels()
        UsageRefreshWorker.schedule(this, intervalMinutes = 60)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
