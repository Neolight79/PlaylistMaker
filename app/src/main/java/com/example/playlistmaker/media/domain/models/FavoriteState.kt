package com.example.playlistmaker.media.domain.models

import com.example.playlistmaker.search.domain.models.Track

sealed interface FavoriteState {

    data object Loading : FavoriteState

    data class TracksFavorite(
        val trackList: List<Track>
    ) : FavoriteState

    data object Empty : FavoriteState

}
