package com.example.playlistmaker.player.domain.models

import com.example.playlistmaker.search.domain.models.Track

sealed interface PlayerState {

    data object Loading: PlayerState

    data class Content(
        val trackModel: Track,
    ): PlayerState

    data class Error(
        val message: String
    ) : PlayerState

    data class Empty(
        val errorMessage: String
    ) : PlayerState

    data class FavoriteMark(
        val isFavorite: Boolean
    ) : PlayerState

}