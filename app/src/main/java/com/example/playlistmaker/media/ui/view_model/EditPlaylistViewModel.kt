package com.example.playlistmaker.media.ui.view_model

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.Playlist
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistId: Int,
    private val playlistsInteractor: PlaylistsInteractor,
    application: Application) : CreatePlaylistViewModel(playlistsInteractor, application) {

    // Переменная для хранения списка треков плейлиста
    private lateinit var currentPlaylist: Playlist

    init {
        // Заполняем данные плейлиста
        loadPlaylistData()
    }

    // Функция получения данных плейлиста
    private fun loadPlaylistData() {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistData(playlistId).collect { playlistData ->
                currentPlaylist = playlistData.first
                playlistImagePath = currentPlaylist.playlistImagePath
                playlistTitle = currentPlaylist.playlistName
                playlistDescription = currentPlaylist.playlistDescription
                renderState()
            }
        }
    }

    override fun saveNewPlaylist() {
        viewModelScope.launch {
            val playlist = currentPlaylist.copy(
                playlistName = playlistTitle,
                playlistDescription = playlistDescription,
                playlistImagePath = playlistImagePath
            )
            playlistsInteractor.createPlaylist(playlist)
        }

        // Передаём сообщение для вывода при закрытии формы создания плейлиста
        finishLiveData.postValue(getApplication<Application>().resources.getString(
            R.string.playlist_updated_message, playlistTitle))
    }

}