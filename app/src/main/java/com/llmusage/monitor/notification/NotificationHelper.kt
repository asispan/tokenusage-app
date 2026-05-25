package com.llmusage.monitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.llmusage.monitor.MainActivity
import com.llmusage.monitor.R

class NotificationHelper(val context: Context) {

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(
            NotificationChannel(CHANNEL_USAGE, context.getString(R.string.notif_channel_usage), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notif_channel_usage_desc)
            }
        )
        mgr.createNotificationChannel(
            NotificationChannel(CHANNEL_SYNC, context.getString(R.string.notif_channel_sync), NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.notif_channel_sync_desc)
            }
        )
    }

    fun notifyThreshold(providerLabel: String, percent: Int, notifId: Int) {
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_USAGE)
            .setContentTitle("$providerLabel at $percent%")
            .setContentText("You've crossed your $percent% usage threshold.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(notifId, n) }
    }

    companion object {
        const val CHANNEL_USAGE = "usage_alerts"
        const val CHANNEL_SYNC = "background_sync"
    }
}
