package com.example.playlistmaker.sharing.domain

import com.example.playlistmaker.sharing.domain.model.IntentData
import kotlinx.coroutines.flow.Flow

interface SharingInteractor {
    fun shareApp(): IntentData
    fun openTerms(): IntentData
    fun openSupport(): IntentData
    fun sharePlaylist(playlistId: Int): Flow<IntentData>
}