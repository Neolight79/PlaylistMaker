package com.example.playlistmaker.sharing.di

import com.example.playlistmaker.sharing.data.ExternalNavigator
import com.example.playlistmaker.sharing.data.impl.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.impl.SharingInteractorImpl
import org.koin.dsl.module

val sharingModule = module {

    single<ExternalNavigator> {
        ExternalNavigatorImpl()
    }

    factory<SharingInteractor> {
        SharingInteractorImpl(get(), get(), get(), get())
    }

}
