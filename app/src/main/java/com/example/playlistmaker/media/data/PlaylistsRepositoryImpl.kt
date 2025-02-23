package com.example.playlistmaker.media.data

import com.example.playlistmaker.media.data.converters.PlaylistDbConvertor
import com.example.playlistmaker.media.data.converters.TrackInPlaylistsDbConvertor
import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.media.domain.db.PlaylistsRepository
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class PlaylistsRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistDbConvertor: PlaylistDbConvertor,
    private val trackInPlaylistsDbConvertor: TrackInPlaylistsDbConvertor
): PlaylistsRepository {

    override suspend fun insertPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().insertPlaylist(playlistDbConvertor.map(playlist))
    }

    override fun getPlaylists(): Flow<List<Playlist>> = flow {
        val playlists = appDatabase.playlistDao().getPlaylists().first()
        emit(convertFromPlaylistEntity(playlists))
    }

    override fun getPlaylist(playlistId: Int): Flow<Pair<Playlist, List<Track>>> = flow {
        // Получаем и конвертируем плейлист
        val playlist = playlistDbConvertor.map(
            appDatabase.playlistDao().getPlaylist(playlistId).first()
        )
        // На базе списка треков плейлиста получаем и конвертируем список треков
        val tracksList = appDatabase
            .trackInPlaylistsDao()
            .getTracksInPlaylists(playlist.playlistTracks).first().map { track ->
            trackInPlaylistsDbConvertor.map(track)
            }.associateBy {
            it.trackId
        }
        // Получаем список треков отсортированный по списку ID треков в плейлисте
        val sortedTracks = playlist.playlistTracks.mapNotNull {
            tracksList[it]
        }.reversed()
        // Возвращаем в интерактор плейлист и его треки
        emit(Pair(playlist, sortedTracks))
    }

    private fun convertFromPlaylistEntity(playlists: List<PlaylistEntity>): List<Playlist> {
        return playlists.map { playlist -> playlistDbConvertor.map(playlist) }
    }

    override suspend fun addTrackToTracksInPlaylists(track: Track) {
        appDatabase.trackInPlaylistsDao().insertTrack(trackInPlaylistsDbConvertor.map(track))
    }

    override suspend fun deleteTrackFromTracksInPlaylists(track: Track) {
        appDatabase.trackInPlaylistsDao().deleteTrack(track.trackId)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().deletePlaylist(playlist.playlistId)
    }

    override suspend fun cleanUpOrphanedTracks(keepTracksIds: List<Int>) {
        appDatabase.trackInPlaylistsDao().cleanUpOrphanedTracks(keepTracksIds)
    }
}