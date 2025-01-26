package com.example.playlistmaker.player.di

import android.media.MediaPlayer
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {
    viewModel { params ->
        PlayerViewModel(params.get(), get(), get(), get(), get(), androidApplication())
    }

    factory { MediaPlayer() }
}