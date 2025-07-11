package com.example.playlistmaker.search.ui.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.SearchHistory
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.debounce
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val searchInteractor: TracksInteractor,
                      private val searchHistory: SearchHistory,
                      private val application: Application
): ViewModel() {

    // Описание сущностей уровня класса
    companion object {
        const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
    }

    // Описание переменных
    private var latestSearchText: String? = null

    private val trackSearchDebounce = debounce<String>(SEARCH_DEBOUNCE_DELAY_MILLIS, viewModelScope, true) { changedText ->
        if (changedText == latestSearchText) search(changedText)
    }

    // StateFlow для состояния экрана поиска треков (для режима Compose)
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Init)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun searchDebounce(changedText: String) {
        if (latestSearchText != changedText) {
            latestSearchText = changedText
            when (latestSearchText.isNullOrEmpty()) {
                true -> clearSearch()
                false -> trackSearchDebounce(changedText)
            }
        }
    }

    fun searchDirectly(changedText: String) {
        latestSearchText = changedText
        search(changedText)
    }

    private fun search(newSearchText: String) {
        if (newSearchText.isNotEmpty()) {

            renderState(SearchState.Loading)

            viewModelScope.launch {
                searchInteractor.searchTracks(newSearchText).collect { pair ->
                    processResult(pair.first, pair.second)
                }
            }
        } else {
            clearSearch()
        }
    }

    private fun processResult(foundTracks: List<Track>?, errorMessage: String?) {

        val trackList = mutableListOf<Track>()

        if (foundTracks !== null) {
            trackList.addAll(foundTracks)
        }

        when {
            errorMessage != null -> {
                renderState(
                    SearchState.Error(
                        message = application.getString(R.string.errorCommon),
                    )
                )
            }
            trackList.isEmpty() -> {
                renderState(
                    SearchState.Empty(
                        errorMessage = application.getString(R.string.nothing_found),
                    )
                )
            }
            else -> {
                renderState(
                    SearchState.TracksFound(
                        trackList = trackList.toList(),
                    )
                )
            }
        }
    }

    private fun renderState(state: SearchState) {
        _searchState.value = state
    }

    fun clearSearch() {
        renderState(SearchState.TracksFound(listOf()))
        loadHistory()
    }

    private fun renderHistory() {
        viewModelScope.launch {
            searchHistory.getHistory().collect { searchHistoryTrackList ->
                if (searchHistoryTrackList.isNotEmpty())
                    renderState(SearchState.TracksHistory(searchHistoryTrackList))
                else
                    renderState(SearchState.Init)
            }
        }
    }

    fun loadHistory() {
        renderHistory()
    }

    fun clearHistory() {
        searchHistory.clearHistory()
        renderHistory()
    }

    fun addTrack(track: Track) {
        searchHistory.addTrack(track)
    }

}