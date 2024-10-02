package com.example.playlistmaker

import android.content.SharedPreferences
import com.example.playlistmaker.domain.LocalPrefsClient
import com.example.playlistmaker.domain.models.Track

class SearchHistory(private val sharedPrefs: SharedPreferences, private val trackList: MutableList<Track>) {

    private val prefsClient: LocalPrefsClient by lazy {
        Creator.provideSharedPreferences(sharedPrefs)
    }

    init {
        if (trackList.isEmpty()) trackList.addAll(prefsClient.getSearchHistory())
    }

    fun isNotEmpty(): Boolean {
        return trackList.isNotEmpty()
    }

    fun addTrack(track: Track) {

        // Сначала удаляем трек из массива, если он там уже есть
        trackList.remove(track)

        // Добавляем трек в начало списка
        trackList.add(0, track)

        // Обрезаем список до 10 записей
        while (trackList.size > 10)
            trackList.removeLast()

        // Сохраняем список на диск
        saveTrackList()
    }

    fun clearHistory() {

        // Очищаем список
        trackList.clear()

        // Сохраняем в хранилище
        saveTrackList()

    }

    private fun saveTrackList() {
        prefsClient.saveSearchHistory(trackList.toList())
   }

}