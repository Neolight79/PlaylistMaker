package com.example.playlistmaker.sharing.domain.impl

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.db.PlaylistsRepository
import com.example.playlistmaker.settings.data.SettingsRepository
import com.example.playlistmaker.sharing.data.ExternalNavigator
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.model.IntentData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
    private val settingsRepository: SettingsRepository,
    private val playlistsRepository: PlaylistsRepository,
    private val context: Context
) : SharingInteractor {
    override fun shareApp(): IntentData {
        return externalNavigator.shareText(settingsRepository.getShareAppLink())
    }

    override fun openTerms(): IntentData {
        return externalNavigator.openLink(settingsRepository.getTermsLink())
    }

    override fun openSupport(): IntentData {
        return externalNavigator.openEmail(settingsRepository.getSupportEmailData())
    }

    override fun sharePlaylist(playlistId: Int): Flow<IntentData> = flow {
        val playlistData = playlistsRepository.getPlaylist(playlistId).first()
        val playlistText = StringBuilder()
        playlistText
            .append(context.getString(R.string.playlist_text_title, playlistData.first.playlistName, playlistData.first.playlistDescription))
            .append(
                with (playlistData.first) {
                    when {
                        playlistTracksQuantity % 10 == 1 && playlistTracksQuantity % 100 != 11 ->
                            context.getString(R.string.tracks_quantity_1, playlistTracksQuantity)
                        playlistTracksQuantity % 10 in 2..4 && playlistTracksQuantity % 100 !in 12..14 ->
                            context.getString(R.string.tracks_quantity_2, playlistTracksQuantity)
                        else ->
                            context.getString(R.string.tracks_quantity, playlistTracksQuantity)
                    }
                })
        var i = 1
        playlistData.second.forEach {
            playlistText.append(context.getString(R.string.playlist_text_item, i++, it.artistName, it.trackName, it.trackTimeString))
        }
        emit(externalNavigator.shareText(playlistText.toString()))
    }

}