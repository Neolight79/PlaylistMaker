package com.example.playlistmaker.search.domain.models

sealed interface SearchState {

    data object Loading : SearchState

    data class TracksFound(
        val trackList: List<Track>
    ) : SearchState

    data class TracksHistory(
        val trackList: List<Track>
    ) : SearchState

    data class Error(
        val message: String
    ) : SearchState

    data class Empty(
        val errorMessage: String
    ) : SearchState

}
