package com.example.playlistmaker.search.data.dto

sealed interface ItunesRequest {
    data class GetTracks(val expression: String) : ItunesRequest
    data class GetTrackData(val trackId: Int) : ItunesRequest
}
