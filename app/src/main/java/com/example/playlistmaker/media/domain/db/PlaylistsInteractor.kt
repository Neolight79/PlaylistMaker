package com.example.playlistmaker.media.domain.db

import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    suspend fun createPlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistData(playlistId: Int): Flow<Pair<Playlist, List<Track>>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    suspend fun deleteTrackFromPlaylist(track: Track, playlist: Playlist)
    fun deletePlaylist(playlist: Playlist): Flow<Unit>
}