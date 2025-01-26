package com.example.playlistmaker.media.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.domain.models.PlaylistsState
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var playlistsStateLiveData = MutableLiveData<PlaylistsState>()
    fun observeState(): LiveData<PlaylistsState> = playlistsStateLiveData

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
        playlistsStateLiveData.postValue(state)
    }

    fun openPlaylist(playlist: Playlist) {
        // ToDo Необходимо реализовать функционал открытия плейлиста
    }

}