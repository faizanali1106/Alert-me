package com.alertrelay.app.data

import android.content.Context
import androidx.core.content.edit

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var ntfyTopic: String
        get() = prefs.getString(KEY_TOPIC, "") ?: ""
        set(value) = prefs.edit { putString(KEY_TOPIC, value.trim()) }

    var relayEnabled: Boolean
        get() = prefs.getBoolean(KEY_RELAY_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_RELAY_ENABLED, value) }

    var includeDetails: Boolean
        get() = prefs.getBoolean(KEY_INCLUDE_DETAILS, true)
        set(value) = prefs.edit { putBoolean(KEY_INCLUDE_DETAILS, value) }

    var themeMode: ThemeMode
        get() = ThemeMode.entries.getOrElse(prefs.getInt(KEY_THEME, 0)) { ThemeMode.SYSTEM }
        set(value) = prefs.edit { putInt(KEY_THEME, value.ordinal) }

    companion object {
        private const val PREFS_NAME = "alert_relay_prefs"
        private const val KEY_TOPIC = "ntfy_topic"
        private const val KEY_RELAY_ENABLED = "relay_enabled"
        private const val KEY_INCLUDE_DETAILS = "include_details"
        private const val KEY_THEME = "theme_mode"
    }
}
