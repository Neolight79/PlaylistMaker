package com.example.playlistmaker.media.ui.view_model

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.sharing.domain.SharingInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistId: Int,
    private val playlistsInteractor: PlaylistsInteractor,
    private val sharingInteractor: SharingInteractor,
    private val application: Application
): ViewModel() {

    // StateFlow для состояния экрана избранных треков (для режима Compose)
    private val _playlistState = MutableStateFlow<Pair<Playlist, List<Track>>>(Pair(Playlist(), listOf()))
    val playlistState: StateFlow<Pair<Playlist, List<Track>>> = _playlistState.asStateFlow()

    // StateFlow для вызова внешних интентов (для режима Compose)
    private val _stateIntentCompose = MutableStateFlow<Intent>(Intent())
    val stateIntentCompose = _stateIntentCompose.asStateFlow()

    // StateFlow для строки сообщения перед закрытием окна создания/изменения плейлиста
    // (true - выйти после показа, false - не выходить после показа)
    private val _stateFlowMessage = MutableStateFlow<Pair<String, Boolean>>(Pair("", false))
    val stateFlowMessage: StateFlow<Pair<String, Boolean>> = _stateFlowMessage.asStateFlow()

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
                _stateFlowMessage.value = Pair(application.getString(R.string.playlist_deleted_message, currentPlaylist.playlistName), true)
            }
        }
    }

    // Функция отправки данных плейлиста на фрагмент
    private fun renderState(state: Pair<Playlist, List<Track>>) {
        _playlistState.value = state
    }

    // Функции передачи интента в StateFlow для выполнения внешних операций
    fun sharePlaylist() {
        if (currentPlaylist.playlistTracks.isEmpty())
            // Плейлист пустой и делиться нечем, отправляем сообщение
            _stateFlowMessage.value = Pair(application.getString(R.string.empty_playlist_warning), false)
        else
            // Плейлист с данными, получаем текст и отправляем в виде интента
            viewModelScope.launch {
                sharingInteractor.sharePlaylist(currentPlaylist.playlistId).collect { playlistIntent ->
                    _stateIntentCompose.value = playlistIntent.intent
            }
        }
    }
}