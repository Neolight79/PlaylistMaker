package com.example.playlistmaker.media.domain.models

sealed interface PlaylistsState {

    data object Loading : PlaylistsState

    data class Playlists(
        val playlists: List<Playlist>
    ) : PlaylistsState

    data object Empty : PlaylistsState

}
