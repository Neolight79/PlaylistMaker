package com.example.playlistmaker.player.service

import com.example.playlistmaker.player.domain.models.PlayStatus
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerControl {
    fun getCurrentPlayStatus(): StateFlow<PlayStatus>
    fun startPlayer()
    fun pausePlayer()
    fun startForeground()
    fun stopForeground()
}