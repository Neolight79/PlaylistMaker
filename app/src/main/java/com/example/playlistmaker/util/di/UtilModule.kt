package com.example.playlistmaker.util.di

import android.content.Context
import com.example.playlistmaker.PLAYLIST_MAKER_PREFERENCES
import com.example.playlistmaker.util.data.LocalPrefsClientImpl
import com.example.playlistmaker.util.domain.LocalPrefsClient
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {

    single {
        androidContext().getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE)
    }

    factory { Gson() }

    single<LocalPrefsClient> {
        LocalPrefsClientImpl(get(), get())
    }

}
