package com.example.playlistmaker.player.di

import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playerViewModelModule = module {
    viewModel { params ->
        PlayerViewModel(params.get(), get(), androidApplication())
    }
}