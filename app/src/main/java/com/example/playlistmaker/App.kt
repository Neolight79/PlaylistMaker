package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.LocalPrefsClient

const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"

class App : Application() {

    private lateinit var sharedPrefs: SharedPreferences
    var darkTheme = false
        private set

    private val prefsClient: LocalPrefsClient by lazy {
        Creator.provideSharedPreferences(sharedPrefs)
    }

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        darkTheme = prefsClient.isDarkTheme()
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        // Если значение изменилось, то сохраняем новое в SharedPreferences
        if (darkTheme != darkThemeEnabled)
            prefsClient.saveDarkTheme(darkThemeEnabled)

        // В любом случае выставляем значение в соответствии с параметром
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
