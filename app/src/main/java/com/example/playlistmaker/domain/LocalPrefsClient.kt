package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.models.Track

interface LocalPrefsClient {

    // Функции для сохранения и восстановления списка треков
    fun getSearchHistory(): List<Track>
    fun saveSearchHistory(tracksList: List<Track>)

    // Функции для сохранения и восстановления признака тёмной темы
    fun isDarkTheme(): Boolean
    fun saveDarkTheme(isDarkTheme: Boolean)

}