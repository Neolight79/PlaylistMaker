package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.domain.models.Track

interface SearchHistory {
    fun isNotEmpty(): Boolean
    fun addTrack(track: Track)
    fun clearHistory()
    fun getHistory(): List<Track>
}