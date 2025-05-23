package com.example.playlistmaker.media.domain.db

import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    suspend fun insertPlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylist(playlistId: Int): Flow<Pair<Playlist, List<Track>>>
    suspend fun addTrackToTracksInPlaylists(track: Track)
    suspend fun deleteTrackFromTracksInPlaylists(track: Track)
    suspend fun cleanUpOrphanedTracks(keepTracksIds: List<Int>)
    suspend fun deletePlaylist(playlist: Playlist)
}