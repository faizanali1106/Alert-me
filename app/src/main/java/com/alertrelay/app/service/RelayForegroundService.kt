package com.alertrelay.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.alertrelay.app.AlertRelayApp
import com.alertrelay.app.MainActivity
import com.alertrelay.app.R
import com.alertrelay.app.ntfy.AlertSender
import com.alertrelay.app.ntfy.AlertType
import com.alertrelay.app.util.ContactResolver
import com.alertrelay.app.util.NotificationChannels
import java.util.concurrent.Executor

class RelayForegroundService : Service() {
    private var telephonyManager: TelephonyManager? = null
    private var legacyListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationChannels.FOREGROUND_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NotificationChannels.FOREGROUND_ID, notification)
        }
        registerCallListener()
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterCallListener()
        super.onDestroy()
    }

    private fun registerCallListener() {
        if (!hasPhonePermission()) return
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager = tm
        unregisterCallListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        onIncomingCall(null)
                    }
                }
            }
            telephonyCallback = callback
            val executor = Executor { it.run() }
            tm.registerTelephonyCallback(executor, callback)
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        onIncomingCall(phoneNumber)
                    }
                }
            }
            legacyListener = listener
            @Suppress("DEPRECATION")
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun onIncomingCall(phoneNumber: String?) {
        val prefs = (applicationContext as AlertRelayApp).prefs
        // Caller name/number comes from the Phone app notification when telephony has no number.
        if (prefs.includeDetails && phoneNumber.isNullOrBlank()) {
            return
        }
        val detail = if (prefs.includeDetails && !phoneNumber.isNullOrBlank()) {
            ContactResolver.resolveDisplayName(this, phoneNumber)
        } else {
            null
        }
        AlertSender.send(this, AlertType.CALL, detail)
    }

    private fun unregisterCallListener() {
        val tm = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { tm.unregisterTelephonyCallback(it) }
        } else {
            @Suppress("DEPRECATION")
            legacyListener?.let { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
        }
        telephonyCallback = null
        legacyListener = null
    }

    private fun hasPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildNotification(): Notification {
        val prefs = (applicationContext as AlertRelayApp).prefs
        val body = if (prefs.includeDetails) {
            getString(R.string.foreground_notification_text_details)
        } else {
            getString(R.string.foreground_notification_text)
        }
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NotificationChannels.FOREGROUND_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_relay)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(body)
            .setOngoing(true)
            .setContentIntent(openApp)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        fun start(context: android.content.Context) {
            val intent = Intent(context, RelayForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, RelayForegroundService::class.java))
        }

        fun sync(context: android.content.Context) {
            val prefs = (context.applicationContext as AlertRelayApp).prefs
            if (prefs.relayEnabled) {
                start(context)
            } else {
                stop(context)
            }
        }
    }
}
