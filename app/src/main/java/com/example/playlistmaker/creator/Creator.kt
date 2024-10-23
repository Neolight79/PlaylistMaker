package com.example.playlistmaker.creator

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.util.data.LocalPrefsClientImpl
import com.example.playlistmaker.search.data.TracksRepositoryImpl
import com.example.playlistmaker.search.data.network.RetrofitNetworkClient
import com.example.playlistmaker.util.domain.LocalPrefsClient
import com.example.playlistmaker.search.domain.SearchHistory
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.api.TracksRepository
import com.example.playlistmaker.search.domain.impl.SearchHistoryImpl
import com.example.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.settings.data.SettingsRepository
import com.example.playlistmaker.settings.data.impl.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.sharing.data.ExternalNavigator
import com.example.playlistmaker.sharing.data.impl.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.impl.SharingInteractorImpl

object Creator {

    private lateinit var prefsClient: LocalPrefsClient

    // Функции получения инструментов для поиска треков
    fun provideTracksInteractor(context: Context): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository(context))
    }

    private fun getTracksRepository(context: Context): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient(context))
    }

    // Функции получения инструментов для работы с настройками
    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository(context))
    }

    private fun getSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    // Функции получения инструментов для работы с интентами
    fun provideSharingInteractor(context: Context): SharingInteractor {
        return SharingInteractorImpl(getExternalNavigator(), getSettingsRepository(context))
    }

    private fun getExternalNavigator(): ExternalNavigator {
        return ExternalNavigatorImpl()
    }

    // Функция получения инструмента работы с локальными настройками
    fun provideSharedPreferences(sharedPreferences: SharedPreferences): LocalPrefsClient {
        prefsClient = LocalPrefsClientImpl(sharedPreferences)
        return prefsClient
    }

    // Функция получения инструмента работы с историей поиска
    fun provideSearchHistory(trackList: MutableList<Track>): SearchHistory {
        return SearchHistoryImpl(prefsClient, trackList)
    }

}