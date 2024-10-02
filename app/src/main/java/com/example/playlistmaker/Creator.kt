package com.example.playlistmaker

import android.content.SharedPreferences
import com.example.playlistmaker.data.LocalPrefsClientImpl
import com.example.playlistmaker.data.TracksRepositoryImpl
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.domain.LocalPrefsClient
import com.example.playlistmaker.domain.SearchHistory
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.impl.SearchHistoryImpl
import com.example.playlistmaker.domain.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.models.Track

object Creator {

    lateinit var prefsClient: LocalPrefsClient

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSharedPreferences(sharedPreferences: SharedPreferences): LocalPrefsClient {
        prefsClient = LocalPrefsClientImpl(sharedPreferences)
        return prefsClient
    }

    fun provideSearchHistory(trackList: MutableList<Track>): SearchHistory {
        return SearchHistoryImpl(prefsClient, trackList)
    }
}