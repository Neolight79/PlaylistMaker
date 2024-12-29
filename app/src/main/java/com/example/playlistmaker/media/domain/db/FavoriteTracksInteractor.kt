package com.example.playlistmaker.media.domain.db

import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun saveTrackToFavorite(track: Track)
    suspend fun deleteTrackFromFavorite(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
}