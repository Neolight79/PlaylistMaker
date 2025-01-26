package com.example.playlistmaker.media.domain.db.impl

import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.db.PlaylistsRepository
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistsInteractorImpl(
    private val playlistsRepository: PlaylistsRepository
): PlaylistsInteractor {

    override suspend fun createPlaylist(playlist: Playlist) {
        playlistsRepository.insertPlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistsRepository.getPlaylists().map {
            it.reversed()
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        // Добавляем в плейлист новый трек и инкрементируем счётчик треков
        val newPlaylist = playlist.copy (
            playlistTracks = playlist.playlistTracks.plus(track.trackId),
            playlistTracksQuantity = playlist.playlistTracksQuantity + 1
        )
        // Обновляем запись в БД о плейлисте
        playlistsRepository.insertPlaylist(newPlaylist)
        // Отправляем трек для сохранения в таблице треков в плейлистах
        playlistsRepository.addTrackToTracksInPlaylists(track)
    }

}