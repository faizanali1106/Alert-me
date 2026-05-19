package com.alertrelay.app

import android.app.Application
import com.alertrelay.app.data.PrefsManager
import com.alertrelay.app.util.ThemeHelper

class AlertRelayApp : Application() {
    lateinit var prefs: PrefsManager
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)
        ThemeHelper.apply(prefs.themeMode)
    }
}
