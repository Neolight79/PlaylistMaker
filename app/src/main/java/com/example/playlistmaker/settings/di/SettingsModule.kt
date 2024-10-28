package com.example.playlistmaker.settings.di

import com.example.playlistmaker.settings.data.SettingsRepository
import com.example.playlistmaker.settings.data.impl.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsDataModule = module {

    single<SettingsRepository> {
        SettingsRepositoryImpl(androidContext())
    }

}

val settingsDomainModule = module {

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

}

val settingsViewModelModule = module {
    viewModel {
        SettingsViewModel(get(), get(), androidApplication())
    }
}