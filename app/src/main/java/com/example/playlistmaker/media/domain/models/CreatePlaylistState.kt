package com.example.playlistmaker.media.domain.models

data class ManagePlaylistState(
    val isSavable: Boolean = false,
    val isFilled: Boolean = false,
    val isNewPlaylist: Boolean = true,
    val playlistImagePath: String = "",
    val playlistTitle: String = "",
    val playlistDescription: String = "")
