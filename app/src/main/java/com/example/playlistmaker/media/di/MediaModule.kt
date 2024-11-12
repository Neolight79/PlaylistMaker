package com.example.playlistmaker.media.di

import com.example.playlistmaker.media.ui.view_model.FavoriteViewModel
import com.example.playlistmaker.media.ui.view_model.PlaylistsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    viewModel {
        FavoriteViewModel()
    }

    viewModel {
        PlaylistsViewModel()
    }

}