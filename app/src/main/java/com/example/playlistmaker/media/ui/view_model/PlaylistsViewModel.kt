package com.example.playlistmaker.media.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.PlaylistsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    // StateFlow для состояния экрана плейлистов (для режима Compose)
    private val _playlistsState = MutableStateFlow<PlaylistsState>(PlaylistsState.Loading)
    val playlistsState: StateFlow<PlaylistsState> = _playlistsState.asStateFlow()

    init {
        fillData()
    }

    fun fillData() {
        renderState(PlaylistsState.Loading)
        viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                if (playlists.isEmpty())
                    renderState(PlaylistsState.Empty)
                else
                    renderState(PlaylistsState.Playlists(playlists))
            }
        }
    }

    private fun renderState(state: PlaylistsState) {
        _playlistsState.value = state
    }
}