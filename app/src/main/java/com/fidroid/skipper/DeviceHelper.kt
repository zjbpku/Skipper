package com.fidroid.skipper

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

fun isAccessibilitySettingsEnabled(context: Context, clazz: Class<out AccessibilityService?>): Boolean {
    try {
        var accessibilityEnabled = 0
        val service: String = context.packageName.toString() + "/" + clazz.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.applicationContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue: String = Settings.Secure.getString(context.applicationContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue.isNullOrEmpty()) {
                return false
            }
            mStringColonSplitter.setString(settingValue)
            return !mStringColonSplitter.find { it.lowercase() == service.lowercase() }.isNullOrEmpty()
        }
        return false
    } catch (e: Exception) {
        return false
    }

}