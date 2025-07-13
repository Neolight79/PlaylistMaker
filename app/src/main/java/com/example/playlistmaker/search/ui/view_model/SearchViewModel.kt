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
    private var latestSearchText = ""
    private var isDirectSearchRun = false

    private val trackSearchDebounce = debounce<String>(SEARCH_DEBOUNCE_DELAY_MILLIS, viewModelScope, true) { changedText ->
        if (!isDirectSearchRun) search(changedText)
    }

    // StateFlow для состояния экрана поиска треков
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Init)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // StateFlow для строки поиска
    private val _searchTextState = MutableStateFlow(latestSearchText)
    val searchTextState: StateFlow<String> = _searchTextState.asStateFlow()

    fun searchDebounce(changedText: String) {
        if (latestSearchText != changedText) {
            latestSearchText = changedText
            _searchTextState.value = changedText
            when (changedText.isEmpty()) {
                true -> clearSearch()
                false -> {
                    trackSearchDebounce(changedText)
                    isDirectSearchRun = false
                }
            }
        }
    }

    fun searchDirectly() {
        search(latestSearchText)
        isDirectSearchRun = true
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
        latestSearchText = ""
        _searchTextState.value = ""
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