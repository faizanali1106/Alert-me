package com.alertrelay.app.util

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification

object NotificationContentExtractor {
    private val IGNORE_TITLES = setOf(
        "phone",
        "dialer",
        "messages",
        "messaging",
        "message",
        "sms",
        "text",
        "incoming call",
        "ongoing call",
        "missed call",
    )

    fun extractSender(sbn: StatusBarNotification): String? {
        val extras = sbn.notification.extras ?: return null
        val title = readChar(extras, Notification.EXTRA_TITLE)
        val conversation = readChar(extras, Notification.EXTRA_CONVERSATION_TITLE)
        val subtitle = readChar(extras, Notification.EXTRA_SUB_TEXT)

        val candidate = when {
            !conversation.isNullOrBlank() && conversation != title -> conversation
            !title.isNullOrBlank() -> title
            !subtitle.isNullOrBlank() -> subtitle
            else -> null
        }
        return sanitize(candidate)
    }

    fun extractCaller(sbn: StatusBarNotification): String? {
        val extras = sbn.notification.extras ?: return null
        val title = readChar(extras, Notification.EXTRA_TITLE)
        val text = readChar(extras, Notification.EXTRA_TEXT)
        val sub = readChar(extras, Notification.EXTRA_SUB_TEXT)

        // Many dialers use title "Incoming call" and put the name in text/subtitle.
        // Sanitize each field in order instead of picking the first non-blank raw value.
        for (raw in listOf(title, text, sub)) {
            sanitize(raw)?.let { return it }
        }
        return null
    }

    fun isSmsNotification(sbn: StatusBarNotification): Boolean {
        return SMS_PACKAGES.contains(sbn.packageName)
    }

    fun isIncomingCallNotification(sbn: StatusBarNotification): Boolean {
        if (!DIALER_PACKAGES.contains(sbn.packageName)) return false
        val category = sbn.notification.category
        if (category == Notification.CATEGORY_CALL) return true
        val extras = sbn.notification.extras ?: return false
        val title = readChar(extras, Notification.EXTRA_TITLE)?.lowercase().orEmpty()
        val text = readChar(extras, Notification.EXTRA_TEXT)?.lowercase().orEmpty()
        return title.contains("incoming") ||
            text.contains("incoming") ||
            text.contains("ringing") ||
            title.isNotBlank() && !IGNORE_TITLES.contains(title)
    }

    private fun readChar(extras: Bundle, key: String): String? {
        return extras.getCharSequence(key)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun sanitize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (cleaned.length < 2) return null
        if (IGNORE_TITLES.contains(cleaned.lowercase())) return null
        if (cleaned.matches(Regex("^\\d+\\s+new messages?$", RegexOption.IGNORE_CASE))) return null
        return cleaned.take(80)
    }

    val SMS_PACKAGES = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.android.messaging",
        "com.microsoft.android.smsorganizer",
        "org.thoughtcrime.securesms",
        "com.verizon.messaging.vzmsgs",
    )

    val DIALER_PACKAGES = setOf(
        "com.google.android.dialer",
        "com.android.dialer",
        "com.samsung.android.dialer",
        "com.samsung.android.incallui",
        "com.android.server.telecom",
        "com.sh.smart.caller",
        "com.oneplus.dialer",
        "com.miui.dialer",
        "com.huawei.contacts",
    )
}
