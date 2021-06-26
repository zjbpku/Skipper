package com.fidroid.skipper.service

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService
import com.fidroid.skipper.isAccessibilitySettingsEnabled

class QuickTileService : TileService() {

    override fun onClick() {
        super.onClick()
        if (!isAccessibilitySettingsEnabled(this, SkipperService::class.java)) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onStartListening() {
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onTileAdded() {
        super.onTileAdded()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }
}