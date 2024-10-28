package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.player.di.playerViewModelModule
import com.example.playlistmaker.search.di.searchDataModule
import com.example.playlistmaker.search.di.searchDomainModule
import com.example.playlistmaker.search.di.searchViewModelModule
import com.example.playlistmaker.settings.di.settingsDataModule
import com.example.playlistmaker.settings.di.settingsDomainModule
import com.example.playlistmaker.settings.di.settingsViewModelModule
import com.example.playlistmaker.sharing.di.sharingDataModule
import com.example.playlistmaker.sharing.di.sharingDomainModule
import com.example.playlistmaker.util.di.utilDataModule
import com.example.playlistmaker.util.domain.LocalPrefsClient
import com.example.playlistmaker.util.di.utilDomainModule
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
            modules(playerViewModelModule,
                searchDataModule, searchDomainModule, searchViewModelModule,
                settingsDataModule, settingsDomainModule, settingsViewModelModule,
                sharingDataModule, sharingDomainModule,
                utilDataModule, utilDomainModule
            )
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
