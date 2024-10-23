package com.example.playlistmaker.sharing.domain

import com.example.playlistmaker.sharing.domain.model.IntentData

interface SharingInteractor {
    fun shareApp(): IntentData
    fun openTerms(): IntentData
    fun openSupport(): IntentData
}