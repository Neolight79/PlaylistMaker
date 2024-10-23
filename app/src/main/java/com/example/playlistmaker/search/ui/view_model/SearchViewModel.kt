package com.example.playlistmaker.search.ui.view_model

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track

class SearchViewModel(application: Application): AndroidViewModel(application) {

    // Описание сущностей уровня класса
    companion object {

        private val SEARCH_REQUEST_TOKEN = Any()
        const val SEARCH_DEBOUNCE_DELAY = 2000L

        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SearchViewModel(this[APPLICATION_KEY] as Application)
            }
        }
    }

    // Описание переменных
    private var mainThreadHandler = Handler(Looper.getMainLooper())
    private var latestSearchText: String? = null

    private val stateLiveData = MutableLiveData<SearchState>()
    fun observeState(): LiveData<SearchState> = stateLiveData

    private val searchInteractor = Creator.provideTracksInteractor(getApplication())

    private val searchHistoryTrackList = mutableListOf<Track>()
    private val searchHistory = Creator.provideSearchHistory(searchHistoryTrackList)

    // Описание методов
    override fun onCleared() {
        mainThreadHandler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) {
            return
        }

        this.latestSearchText = changedText
        mainThreadHandler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)

        val searchRunnable = Runnable { search(changedText) }

        val postTime = SystemClock.uptimeMillis() + SEARCH_DEBOUNCE_DELAY
        mainThreadHandler.postAtTime(
            searchRunnable,
            SEARCH_REQUEST_TOKEN,
            postTime
        )
    }

    fun searchDirectly(changedText: String) {
        this.latestSearchText = changedText
        mainThreadHandler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
        search(changedText)
    }

    private fun search(newSearchText: String) {
        if (newSearchText.isNotEmpty()) {
            renderState(SearchState.Loading)

            searchInteractor.searchTracks(newSearchText, object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                        val trackList = mutableListOf<Track>()
                        if (foundTracks != null) {
                            trackList.addAll(foundTracks)

                        when {
                            errorMessage != null -> {
                                renderState(
                                    SearchState.Error(
                                        message = getApplication<Application>().getString(R.string.errorCommon),
                                    )
                                )
                            }

                            trackList.isEmpty() -> {
                                renderState(
                                    SearchState.Empty(
                                        errorMessage = getApplication<Application>().getString(R.string.nothing_found),
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
                }
            })
        }
    }

    private fun renderState(state: SearchState) {
        stateLiveData.postValue(state)
    }

    fun clearSearch() {
        mainThreadHandler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
        renderState(SearchState.TracksFound(listOf()))
        loadHistory()
    }

    private fun renderHistory() {
        renderState(SearchState.TracksHistory(searchHistoryTrackList.toList()))
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
        renderHistory()
    }

}