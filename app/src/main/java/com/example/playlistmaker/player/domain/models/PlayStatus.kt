package com.example.playlistmaker.player.domain.models

data class PlayStatus(
    val currentPosition: String = "00:00",
    val isPlaying: Boolean = false
)
