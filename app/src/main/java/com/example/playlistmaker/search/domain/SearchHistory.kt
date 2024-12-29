package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistory {
    fun isNotEmpty(): Boolean
    fun addTrack(track: Track)
    fun clearHistory()
    fun getHistory(): Flow<List<Track>>
}