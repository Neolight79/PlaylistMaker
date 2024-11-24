package com.example.playlistmaker.media.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaylistsViewModel : ViewModel() {

    private var playlistsStateLiveData = MutableLiveData<String>()
    fun observeState(): LiveData<String> = playlistsStateLiveData

}