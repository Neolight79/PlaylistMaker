package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.models.Track

interface SearchHistory {
    fun isNotEmpty(): Boolean
    fun addTrack(track: Track)
    fun clearHistory()
}