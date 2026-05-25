package com.llmusage.monitor.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.llmusage.monitor.LLMUsageApp
import java.util.concurrent.TimeUnit

class UsageRefreshWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as LLMUsageApp).container
        return runCatching { container.syncManager.syncAllOnce() }
            .map { Result.success() }
            .getOrElse { Result.retry() }
    }

    companion object {
        private const val NAME = "llm-usage-refresh"

        fun schedule(context: Context, intervalMinutes: Int) {
            val interval = intervalMinutes.coerceAtLeast(15).toLong()
            val req = PeriodicWorkRequestBuilder<UsageRefreshWorker>(interval, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NAME)
        }
    }
}
