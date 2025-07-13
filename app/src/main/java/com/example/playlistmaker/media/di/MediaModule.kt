package com.example.playlistmaker.media.di

import androidx.room.Room
import com.example.playlistmaker.media.data.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.media.data.PlaylistsRepositoryImpl
import com.example.playlistmaker.media.data.converters.PlaylistDbConvertor
import com.example.playlistmaker.media.data.converters.TrackDbConvertor
import com.example.playlistmaker.media.data.converters.TrackInPlaylistsDbConvertor
import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.db.FavoriteTracksRepository
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.db.PlaylistsRepository
import com.example.playlistmaker.media.domain.db.impl.FavoriteTracksInteractorImpl
import com.example.playlistmaker.media.domain.db.impl.PlaylistsInteractorImpl
import com.example.playlistmaker.media.ui.view_model.FavoriteViewModel
import com.example.playlistmaker.media.ui.view_model.ManagePlaylistViewModel
import com.example.playlistmaker.media.ui.view_model.PlaylistViewModel
import com.example.playlistmaker.media.ui.view_model.PlaylistsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    viewModel {
        FavoriteViewModel(get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel { params ->
        ManagePlaylistViewModel(params.get(), get(), androidApplication())
    }

    viewModel { params ->
        PlaylistViewModel(params.get(), get(), get(), androidApplication())
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration(true)
            .build()
    }

    factory { TrackDbConvertor() }

    factory { PlaylistDbConvertor() }

    factory { TrackInPlaylistsDbConvertor() }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get(), get())
    }

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(get(), get(), get())
    }

    single<FavoriteTracksInteractor> {
        FavoriteTracksInteractorImpl(get())
    }

    single<PlaylistsInteractor> {
        PlaylistsInteractorImpl(get())
    }

}