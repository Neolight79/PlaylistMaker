package com.example.playlistmaker.util.data

import android.content.SharedPreferences
import com.example.playlistmaker.util.domain.LocalPrefsClient
import com.example.playlistmaker.search.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val NIGHT_MODE_STATE = "night_mode"
const val SEARCH_HISTORY_KEY = "search_history"

class LocalPrefsClientImpl(private val sharedPreferences: SharedPreferences,
                           private val gson: Gson): LocalPrefsClient {

    override fun getSearchHistory(): List<Track> {
        val tracksJson: String? = sharedPreferences.getString(SEARCH_HISTORY_KEY, "")
        return if (tracksJson.isNullOrEmpty()) {
            listOf()
        } else {
            class Token : TypeToken<List<Track>>()
            return gson.fromJson(tracksJson, Token().type)
        }
    }

    override fun saveSearchHistory(tracksList: List<Track>) {
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, gson.toJson(tracksList))
            .apply()
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(NIGHT_MODE_STATE, false)
    }

    override fun saveDarkTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit().putBoolean(NIGHT_MODE_STATE, isDarkTheme).apply()
    }

}