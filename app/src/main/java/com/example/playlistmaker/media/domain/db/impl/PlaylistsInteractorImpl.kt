package com.example.playlistmaker.media.domain.db.impl

import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.db.PlaylistsRepository
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

    override fun getPlaylistData(playlistId: Int): Flow<Pair<Playlist, List<Track>>> {
        return playlistsRepository.getPlaylist(playlistId)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        // Добавляем в плейлист новый трек и инкрементируем счётчик треков и длительности
        val newPlaylist = playlist.copy (
            playlistTracks = playlist.playlistTracks.plus(track.trackId),
            playlistTracksQuantity = playlist.playlistTracksQuantity + 1,
            playlistTracksDuration = playlist.playlistTracksDuration + track.trackTimeMillis
        )
        // Обновляем запись в БД о плейлисте
        playlistsRepository.insertPlaylist(newPlaylist)
        // Отправляем трек для сохранения в таблице треков в плейлистах
        playlistsRepository.addTrackToTracksInPlaylists(track)
    }

    override suspend fun deleteTrackFromPlaylist(track: Track, playlist: Playlist) {
        // Удаляем из плейлиста указанный трек, декрементируем счётчик треков и длительности
        val newPlaylist = playlist.copy (
            playlistTracks = playlist.playlistTracks.minus(track.trackId),
            playlistTracksQuantity = playlist.playlistTracksQuantity - 1,
            playlistTracksDuration = playlist.playlistTracksDuration - track.trackTimeMillis
        )
        // Обновляем запись в БД о плейлисте
        playlistsRepository.insertPlaylist(newPlaylist)
        // Выполняем очистку таблицы треков в плейлистах
        val playlists = playlistsRepository.getPlaylists().first().filter {
            it.playlistTracks.contains(track.trackId)
        }
        if (playlists.isEmpty()) playlistsRepository.deleteTrackFromTracksInPlaylists(track)
    }

    override fun deletePlaylist(playlist: Playlist): Flow<Unit> = flow {
        // Сначала удаляем сам плейлист
        playlistsRepository.deletePlaylist(playlist)
        // Раз плейлист удален, то можно продолжить работу приложения
        emit(Unit)
        // Приложение работает дальше, а мы займемся очисткой таблицы треков в плейлистах
        // Сначала получаем все идентификаторы используемых треков и набиваем их в MutableSet
        val keepTracksInPlaylists = mutableSetOf<Int>()
        playlistsRepository.getPlaylists().first().forEach { playlist ->
            keepTracksInPlaylists.addAll(playlist.playlistTracks)
        }
        // Просим репозиторий разобраться с треками, которые не вошли в список в плейлистах
        playlistsRepository.cleanUpOrphanedTracks(keepTracksInPlaylists.toList())
    }
}