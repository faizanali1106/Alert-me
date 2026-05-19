package com.alertrelay.app.ntfy

import android.content.Context
import com.alertrelay.app.AlertRelayApp
import com.alertrelay.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

enum class AlertType {
    CALL,
    SMS,
    TEST_CALL,
    TEST_SMS,
}

object AlertSender {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private const val DEBOUNCE_MS = 12_000L
    private var lastCallSent = 0L
    private var lastSmsSent = 0L

    fun send(context: Context, type: AlertType, detail: String? = null) {
        val app = context.applicationContext as AlertRelayApp
        val prefs = app.prefs
        if (!prefs.relayEnabled) return
        val topic = prefs.ntfyTopic
        if (topic.isBlank()) return

        val now = System.currentTimeMillis()
        val hasDetail = !detail.isNullOrBlank()
        when (type) {
            AlertType.CALL -> {
                if (now - lastCallSent < DEBOUNCE_MS) {
                    // Generic alert may fire before the dialer notification has a name.
                    if (!hasDetail) return
                }
                lastCallSent = now
            }
            AlertType.SMS -> {
                if (now - lastSmsSent < DEBOUNCE_MS) return
                lastSmsSent = now
            }
            else -> { /* tests always allowed */ }
        }

        val (title, body) = buildMessages(context, type, detail, prefs.includeDetails)
        scope.launch {
            NtfyClient.send(topic, title, body)
        }
    }

    suspend fun sendAndWait(
        context: Context,
        type: AlertType,
        detail: String? = null,
    ): Boolean {
        val app = context.applicationContext as AlertRelayApp
        val prefs = app.prefs
        val topic = prefs.ntfyTopic
        if (topic.isBlank()) return false

        val (title, body) = buildMessages(context, type, detail, prefs.includeDetails)
        return NtfyClient.send(topic, title, body)
    }

    private fun buildMessages(
        context: Context,
        type: AlertType,
        detail: String?,
        includeDetails: Boolean,
    ): Pair<String, String> {
        val enriched = detail?.trim()?.takeIf { it.isNotEmpty() }
        val useDetail = includeDetails && enriched != null

        return when (type) {
            AlertType.CALL -> {
                if (useDetail) {
                    context.getString(R.string.alert_call_title) to
                        context.getString(R.string.alert_call_body_named, enriched)
                } else {
                    context.getString(R.string.alert_call_title) to
                        context.getString(R.string.alert_call_body)
                }
            }
            AlertType.SMS -> {
                if (useDetail) {
                    context.getString(R.string.alert_sms_title) to
                        context.getString(R.string.alert_sms_body_named, enriched)
                } else {
                    context.getString(R.string.alert_sms_title) to
                        context.getString(R.string.alert_sms_body)
                }
            }
            AlertType.TEST_CALL -> {
                if (useDetail) {
                    context.getString(R.string.alert_test_call_title) to
                        context.getString(R.string.alert_call_body_named, enriched)
                } else {
                    context.getString(R.string.alert_test_call_title) to
                        context.getString(R.string.alert_test_call_body)
                }
            }
            AlertType.TEST_SMS -> {
                if (useDetail) {
                    context.getString(R.string.alert_test_sms_title) to
                        context.getString(R.string.alert_sms_body_named, enriched)
                } else {
                    context.getString(R.string.alert_test_sms_title) to
                        context.getString(R.string.alert_test_sms_body)
                }
            }
        }
    }
}
