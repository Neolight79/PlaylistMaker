package com.example.playlistmaker.util.di

import android.content.Context
import com.example.playlistmaker.PLAYLIST_MAKER_PREFERENCES
import com.example.playlistmaker.util.data.LocalPrefsClientImpl
import com.example.playlistmaker.util.domain.LocalPrefsClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilDataModule = module {

    single {
        androidContext().getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE)
    }

}

val utilDomainModule = module {

    single<LocalPrefsClient> {
        LocalPrefsClientImpl(get())
    }

}
