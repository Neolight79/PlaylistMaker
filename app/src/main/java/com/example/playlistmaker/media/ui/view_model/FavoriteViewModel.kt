package com.example.playlistmaker.media.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.models.FavoriteState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    // StateFlow для состояния экрана избранных треков
    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState.Loading)
    val favoriteState: StateFlow<FavoriteState> = _favoriteState.asStateFlow()

    fun fillData() {
        renderState(FavoriteState.Loading)
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks().collect { favoriteTracks ->
                if (favoriteTracks.isEmpty())
                    renderState(FavoriteState.Empty)
                else
                    renderState(FavoriteState.TracksFavorite(favoriteTracks))
            }
        }
    }

    init {
        fillData()
    }

    private fun renderState(state: FavoriteState) {
        _favoriteState.value = state
    }

}