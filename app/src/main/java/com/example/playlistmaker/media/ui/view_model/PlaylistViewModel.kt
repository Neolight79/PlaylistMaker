package com.example.playlistmaker.media.ui.view_model

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.settings.ui.view_model.SingleLiveEvent
import com.example.playlistmaker.sharing.domain.SharingInteractor
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistId: Int,
    private val playlistsInteractor: PlaylistsInteractor,
    private val sharingInteractor: SharingInteractor,
    private val application: Application
): ViewModel() {

    // LiveData для передачи данных плейлиста
    private val playlistStateLiveData = MutableLiveData<Pair<Playlist, List<Track>>>()
    fun observePlaylist(): LiveData<Pair<Playlist, List<Track>>> = playlistStateLiveData

    // LiveData для вызова внешних интентов
    private val stateIntent = SingleLiveEvent<Intent>()
    fun observeStateIntent(): LiveData<Intent> = stateIntent

    // LiveData для показа сообщений
    private val stateMessage = SingleLiveEvent<Pair<String, Boolean>>()
    fun observeStateMessage(): LiveData<Pair<String, Boolean>> = stateMessage

    // Переменная для хранения объекта плейлиста
    private lateinit var currentPlaylist: Playlist

    init {
        // Получаем данные плейлиста и список треков
        loadPlaylistData()
    }

    // Функция получения данных плейлиста
    fun loadPlaylistData() {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistData(playlistId).collect { playlistData ->
                currentPlaylist = playlistData.first
                renderState(playlistData)
            }
        }
    }

    // Функция удаления трека из плейлиста
    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            playlistsInteractor.deleteTrackFromPlaylist(track, currentPlaylist)
            loadPlaylistData()
        }
    }

    // Функция удаления плейлиста из плейлиста
    fun deletePlaylist() {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylist(currentPlaylist).collect { _ ->
                stateMessage.value = Pair(application.getString(R.string.playlist_deleted_message, currentPlaylist.playlistName), true)
            }
        }
    }

    // Функция отправки данных плейлиста на фрагмент
    private fun renderState(state: Pair<Playlist, List<Track>>) {
        playlistStateLiveData.postValue(state)
    }

    // Функции передачи интента в LiveData для выполнения внешних операций
    fun sharePlaylist() {
        if (currentPlaylist.playlistTracks.isEmpty())
            // Плейлист пустой и делиться нечем, отправляем сообщение
            stateMessage.value = Pair(application.getString(R.string.empty_playlist_warning), false)
        else
            // Плейлист с данными, получаем текст и отправляем в виде интента
            viewModelScope.launch {
                sharingInteractor.sharePlaylist(currentPlaylist.playlistId).collect { playlistIntent ->
                    stateIntent.value = playlistIntent.intent
            }
        }

    }

}