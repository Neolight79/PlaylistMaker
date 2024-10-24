package com.example.playlistmaker.sharing.domain.impl

import com.example.playlistmaker.settings.data.SettingsRepository
import com.example.playlistmaker.sharing.data.ExternalNavigator
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.model.IntentData

class SharingInteractorImpl(private val externalNavigator: ExternalNavigator, private val settingsRepository: SettingsRepository) : SharingInteractor {
    override fun shareApp(): IntentData {
        return externalNavigator.shareLink(settingsRepository.getShareAppLink())
    }

    override fun openTerms(): IntentData {
        return externalNavigator.openLink(settingsRepository.getTermsLink())
    }

    override fun openSupport(): IntentData {
        return externalNavigator.openEmail(settingsRepository.getSupportEmailData())
    }

}