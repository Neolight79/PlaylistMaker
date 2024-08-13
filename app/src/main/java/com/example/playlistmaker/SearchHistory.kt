package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val SEARCH_HISTORY_KEY = "search_history"

class SearchHistory(private val sharedPrefs: SharedPreferences) {

    var trackList: MutableList<Track>

    init {
        val tracksJson: String? = sharedPrefs.getString(SEARCH_HISTORY_KEY, "")
        trackList = if (tracksJson.isNullOrEmpty()) {
            mutableListOf()
        } else {
            createTrackListFromJson(tracksJson)
        }
    }

    private fun createTrackListFromJson(json: String): MutableList<Track> {
        class Token : TypeToken<MutableList<Track>>()
        return Gson().fromJson(json, Token().type)
    }

    private fun createJsonFromTrackList(tracks: MutableList<Track>): String {
        return Gson().toJson(tracks)
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
        sharedPrefs.edit().putString(SEARCH_HISTORY_KEY, createJsonFromTrackList(trackList))
            .apply()
    }

}