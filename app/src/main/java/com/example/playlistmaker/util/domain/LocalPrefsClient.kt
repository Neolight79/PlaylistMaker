package com.example.playlistmaker.util.domain

import com.example.playlistmaker.search.domain.models.Track

interface LocalPrefsClient {

    // Функции для сохранения и восстановления списка треков
    fun getSearchHistory(): List<Track>
    fun saveSearchHistory(tracksList: List<Track>)

    // Функции для сохранения и восстановления признака тёмной темы
    fun isDarkTheme(default: Boolean): Boolean
    fun saveDarkTheme(isDarkTheme: Boolean)

}