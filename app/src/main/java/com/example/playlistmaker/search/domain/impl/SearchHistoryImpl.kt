package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.util.domain.LocalPrefsClient
import com.example.playlistmaker.search.domain.SearchHistory
import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchHistoryImpl(private val prefsClient: LocalPrefsClient,
                        private val trackList: MutableList<Track>,
                        private val appDatabase: AppDatabase
):
    SearchHistory {

    init {
        if (trackList.isEmpty()) trackList.addAll(prefsClient.getSearchHistory())
    }

    private fun saveTrackList() {
        prefsClient.saveSearchHistory(trackList.toList())
    }

    override fun isNotEmpty(): Boolean {
        return trackList.isNotEmpty()
    }

    override fun addTrack(track: Track) {

        // Сначала удаляем трек из массива, если он там уже есть
        trackList.remove(track)

        // Добавляем трек в начало списка
        trackList.add(0, track)

        // Обрезаем список до 10 записей
        while (trackList.size > 10)
            trackList.removeAt(trackList.lastIndex)

        // Сохраняем список на диск
        saveTrackList()
    }

    override fun clearHistory() {

        // Очищаем список
        trackList.clear()

        // Сохраняем в хранилище
        saveTrackList()

    }

    override fun getHistory(): Flow<List<Track>> = flow {
        val favoriteTrackIds = appDatabase.trackDao().getTrackIds()
        trackList.map {
            it.isFavorite = favoriteTrackIds.contains(it.trackId)
        }
        emit(trackList.toList())
    }

}