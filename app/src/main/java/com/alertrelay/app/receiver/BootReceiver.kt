package com.alertrelay.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alertrelay.app.service.RelayForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        RelayForegroundService.sync(context)
    }
}
