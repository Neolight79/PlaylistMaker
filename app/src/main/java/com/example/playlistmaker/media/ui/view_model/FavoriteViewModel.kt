package com.example.playlistmaker.media.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.models.FavoriteState
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val favoriteStateLiveData = MutableLiveData<FavoriteState>()
    fun observeState(): LiveData<FavoriteState> = favoriteStateLiveData

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
        favoriteStateLiveData.postValue(state)
    }

}