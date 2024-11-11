package com.example.playlistmaker.media.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavoriteViewModel() : ViewModel() {

    private var favoriteStateLiveData = MutableLiveData<String>()
    fun observeState(): LiveData<String> = favoriteStateLiveData

}