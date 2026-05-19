package com.alertrelay.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.alertrelay.app.R

object NotificationChannels {
    const val FOREGROUND_CHANNEL = "alert_relay_service"
    const val FOREGROUND_ID = 1001

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            FOREGROUND_CHANNEL,
            context.getString(R.string.foreground_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.foreground_notification_text)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
