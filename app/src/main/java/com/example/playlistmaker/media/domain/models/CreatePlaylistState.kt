package com.example.playlistmaker.media.domain.models

data class CreatePlaylistState(
    val isSavable: Boolean,
    val isFilled: Boolean,
    val playlistImagePath: String,
    val playlistTitle: String,
    val playlistDescription: String)
