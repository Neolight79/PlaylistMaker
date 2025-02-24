package com.example.playlistmaker.search.ui.view_model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.SearchHistory
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.debounce
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

    // Описание LiveData и обсервера
    private val stateLiveData = MutableLiveData<SearchState>()
    fun observeState(): LiveData<SearchState> = stateLiveData

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
        stateLiveData.postValue(state)
    }

    fun clearSearch() {
        renderState(SearchState.TracksFound(listOf()))
        loadHistory()
    }

    private fun renderHistory() {
        viewModelScope.launch {
            searchHistory.getHistory().collect { searchHistoryTrackList ->
                renderState(SearchState.TracksHistory(searchHistoryTrackList))
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