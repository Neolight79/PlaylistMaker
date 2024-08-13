package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val SEARCH_HISTORY_KEY = "search_history"

class SearchHistory(private val sharedPrefs: SharedPreferences) {

    var trackList = mutableListOf<Track>()
    private val gson = Gson()

    init {
        val tracksJson: String? = sharedPrefs.getString(SEARCH_HISTORY_KEY, "")
        trackList.addAll(if (tracksJson.isNullOrEmpty()) {
            listOf()
        } else {
            createTrackListFromJson(tracksJson)
        })
    }

    fun isNotEmpty(): Boolean {
        return trackList.isNotEmpty()
    }

    private fun createTrackListFromJson(json: String): List<Track> {
        class Token : TypeToken<List<Track>>()
        return gson.fromJson(json, Token().type)
    }

    private fun createJsonFromTrackList(tracks: List<Track>): String {
        return gson.toJson(tracks)
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
        sharedPrefs.edit().putString(SEARCH_HISTORY_KEY, createJsonFromTrackList(trackList.toList()))
            .apply()
    }

}