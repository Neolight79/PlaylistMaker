package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.media.di.mediaModule
import com.example.playlistmaker.player.di.playerModule
import com.example.playlistmaker.search.di.searchModule
import com.example.playlistmaker.settings.di.settingsModule
import com.example.playlistmaker.sharing.di.sharingModule
import com.example.playlistmaker.util.domain.LocalPrefsClient
import com.example.playlistmaker.util.di.utilModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"

class App : Application() {

    private lateinit var sharedPrefs: SharedPreferences
    var darkTheme = false
        private set

    private val prefsClient: LocalPrefsClient by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(mediaModule, playerModule, searchModule, settingsModule, sharingModule, utilModule)
        }
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
