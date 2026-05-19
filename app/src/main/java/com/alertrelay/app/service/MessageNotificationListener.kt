package com.alertrelay.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.alertrelay.app.ntfy.AlertSender
import com.alertrelay.app.ntfy.AlertType
import com.alertrelay.app.util.ContactResolver
import com.alertrelay.app.util.NotificationContentExtractor

class MessageNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        when {
            NotificationContentExtractor.isSmsNotification(sbn) -> handleSms(sbn)
            NotificationContentExtractor.isIncomingCallNotification(sbn) -> handleCall(sbn)
        }
    }

    private fun handleSms(sbn: StatusBarNotification) {
        val category = sbn.notification.category
        if (category != null && category != android.app.Notification.CATEGORY_MESSAGE) {
            return
        }

        if (!shouldSend(sbn.key, SMS_DEBOUNCE_MS)) return

        val sender = NotificationContentExtractor.extractSender(sbn)
        val detail = ContactResolver.enrich(this, sender)
        AlertSender.send(this, AlertType.SMS, detail)
    }

    private fun handleCall(sbn: StatusBarNotification) {
        if (!shouldSend("call:${sbn.key}", CALL_DEBOUNCE_MS)) return

        val caller = NotificationContentExtractor.extractCaller(sbn)
        val detail = ContactResolver.enrich(this, caller)
        AlertSender.send(this, AlertType.CALL, detail)
    }

    private fun shouldSend(key: String, debounceMs: Long): Boolean {
        val now = System.currentTimeMillis()
        synchronized(recentKeys) {
            val last = recentKeys[key]
            if (last != null && now - last < debounceMs) return false
            recentKeys[key] = now
            if (recentKeys.size > 80) {
                val cutoff = now - debounceMs * 2
                recentKeys.entries.removeIf { it.value < cutoff }
            }
        }
        return true
    }

    companion object {
        private const val SMS_DEBOUNCE_MS = 8_000L
        private const val CALL_DEBOUNCE_MS = 12_000L
        private val recentKeys = mutableMapOf<String, Long>()
    }
}
