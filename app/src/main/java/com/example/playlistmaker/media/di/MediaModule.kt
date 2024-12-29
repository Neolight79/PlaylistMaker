package com.example.playlistmaker.media.di

import androidx.room.Room
import com.example.playlistmaker.media.data.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.media.data.converters.TrackDbConvertor
import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.db.FavoriteTracksRepository
import com.example.playlistmaker.media.domain.db.impl.FavoriteTracksInteractorImpl
import com.example.playlistmaker.media.ui.view_model.FavoriteViewModel
import com.example.playlistmaker.media.ui.view_model.PlaylistsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    viewModel {
        FavoriteViewModel(get())
    }

    viewModel {
        PlaylistsViewModel()
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .build()
    }

    factory { TrackDbConvertor() }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get(), get())
    }

    single<FavoriteTracksInteractor> {
        FavoriteTracksInteractorImpl(get())
    }

}