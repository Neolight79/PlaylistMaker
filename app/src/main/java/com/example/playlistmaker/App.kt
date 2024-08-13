package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"
const val NIGHT_MODE_STATE = "night_mode"

class App : Application() {

    lateinit var sharedPrefs: SharedPreferences
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(NIGHT_MODE_STATE, false)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        // Если значение изменилось, то сохраняем новое в SharedPreferences
        if (darkTheme != darkThemeEnabled)
            sharedPrefs.edit().putBoolean(NIGHT_MODE_STATE, darkThemeEnabled).apply()

        // В любом случае выставляем значение в соответствии с папаметром
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
