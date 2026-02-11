package com.example.fzo.data

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("fzo_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val autoPlayAll = prefs.getBoolean("autoPlayAll", true)
        val shuffleEnabled = prefs.getBoolean("shuffleEnabled", false)
        val volume = prefs.getFloat("volume", 1f)
        return AppSettings(autoPlayAll, shuffleEnabled, volume)
    }

    fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean("autoPlayAll", settings.autoPlayAll)
            .putBoolean("shuffleEnabled", settings.shuffleEnabled)
            .putFloat("volume", settings.volume)
            .apply()
    }
}
