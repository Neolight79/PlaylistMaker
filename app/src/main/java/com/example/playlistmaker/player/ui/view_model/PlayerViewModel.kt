package com.example.playlistmaker.player.ui.view_model

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import com.example.playlistmaker.R
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.Track
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.BottomSheetState
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.player.domain.models.PlayStatus
import com.example.playlistmaker.player.service.AudioPlayerControl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val trackId: Int,
    private val tracksInteractor: TracksInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: PlaylistsInteractor,
    private val application: Application): ViewModel() {

    // StateFlow для состояния элементов экрана проигрывателя (для режима Compose)
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Loading)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // StateFlow для текущего статуса проигрывания трека (для режима Compose)
    private val _playStatusState = MutableStateFlow<PlayStatus>(PlayStatus())
    val playStatusState: StateFlow<PlayStatus> = _playStatusState.asStateFlow()

    // Переменная для хранения экземпляра интерфейса взаимодействия с сервисом проигрывателя
    private var audioPlayerControl: AudioPlayerControl? = null

    // StateFlow для текущего статуса BottomSheet (для режима Compose)
    @OptIn(ExperimentalMaterial3Api::class)
    private val _bottomSheetState = MutableStateFlow<BottomSheetState>(BottomSheetState(SheetValue.Hidden, null))
    val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState.asStateFlow()

    // StateFlow для текущего статуса BottomSheet (для режима Compose)
    private val _playlistsState = MutableStateFlow<List<Playlist>>(listOf())
    val playlistsState: StateFlow<List<Playlist>> = _playlistsState.asStateFlow()

    // Переменная для хранения объекта трека для работы с избранным
    private lateinit var currentTrack: Track

    init {
        // Загружаем данные трека из сети
        loadTrackData()
    }

    // Функция загрузки данных трека из сети через интерактор
    fun loadTrackData() {
        viewModelScope.launch {
            tracksInteractor.loadTrackData(trackId).collect { pair ->
                processResult(pair.first, pair.second)
            }
        }
    }

    // Функция отправки в фрагмент результатов получения данных трека
    private fun processResult(foundTracks: List<Track>?, errorMessage: String?) {
        when {
            errorMessage != null -> {
                renderState(
                    PlayerState.Error(
                        message = application.getString(R.string.errorCommon),
                    )
                )
            }

            foundTracks.isNullOrEmpty() -> {
                renderState(
                    PlayerState.Empty(
                        errorMessage = application.getString(R.string.nothing_found),
                    )
                )
            }

            else -> {
                currentTrack = foundTracks[0]
                renderState(
                    PlayerState.Content(
                        trackModel = currentTrack
                    )
                )
            }
        }
    }

    // Функция переключения режима проигрывателя
    fun playbackControl() {
        when (playStatusState.value.isPlaying) {
            true -> audioPlayerControl?.pausePlayer()
            false -> audioPlayerControl?.startPlayer()
        }
    }

    // Перевод сервиса проигрывателя в режим Foreground
    fun startForegroundPlayerMode() {
        audioPlayerControl?.startForeground()
    }

    // Отключение режима Foreground для проигрывателя
    fun stopForegroundPlayerMode() {
        audioPlayerControl?.stopForeground()
    }

    // Обработка нажатия на кнопку добавления/исключения трека из избранного
    fun onFavoriteClicked() {
        // Выполняем действие
        if (currentTrack.isFavorite)
            viewModelScope.launch {
                favoriteTracksInteractor.deleteTrackFromFavorite(currentTrack)
            }
        else
            viewModelScope.launch {
                favoriteTracksInteractor.saveTrackToFavorite(currentTrack)
            }
        // Меняем признак
        currentTrack = currentTrack.copy(isFavorite = !currentTrack.isFavorite)
        // Отправляем на экран изменения
        renderState(
            PlayerState.Content(trackModel = currentTrack)
        )
    }

    // Обработка нажатия на кнопку добавления трека в плейлист
    @OptIn(ExperimentalMaterial3Api::class)
    fun onAddToPlaylistClicked() {
        // Показываем BottomSheet
        onBottomSheetChangedState(SheetValue.PartiallyExpanded)
    }

    // Обработка нажатия на плейлист для добавления трека
    @OptIn(ExperimentalMaterial3Api::class)
    fun onPlaylistClicked(playlist: Playlist, bottomSheetState: SheetValue) {

        // Проверяем наличие трека в текущем плейлисте и добавляем его, если его там еще нет
        if (playlist.playlistTracks.contains(currentTrack.trackId)) {
            _bottomSheetState.value = BottomSheetState(bottomSheetState,
                application.getString(R.string.exists_in_playlist_message,
                    playlist.playlistName))
        } else {
            viewModelScope.launch {
                playlistsInteractor.addTrackToPlaylist(currentTrack, playlist)
            }
            _bottomSheetState.value = BottomSheetState(SheetValue.Hidden,
                application.getString(R.string.added_to_playlist_message,
                    playlist.playlistName))
            refillPlaylists()
        }

    }

    // Обработка изменения состояние BottomSheet
    @OptIn(ExperimentalMaterial3Api::class)
    fun onBottomSheetChangedState(newState: SheetValue) {
        _bottomSheetState.value = BottomSheetState(newState, null)
    }

    // Обновление списка плейлистов
    fun refillPlaylists() {
        // Получить список плейлистов и отправить на BottomSheet
        viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                renderPlaylists(playlists)
            }
        }
    }

    // Функция очистки перед закрытием
    override fun onCleared() {
        super.onCleared()
        removeAudioPlayerControl()
    }

    // Функция отправки статуса экрана на View
    private fun renderState(state: PlayerState) {
        _playerState.value = state
    }

    // Функция отправки списка плейлистов
    private fun renderPlaylists(playlists: List<Playlist>) {
        _playlistsState.value = playlists
    }

    // Привязка экземпляра интерфейса для управления проигрывателем в сервисе
    fun setAudioPlayerControl(audioPlayerControl: AudioPlayerControl) {
        this.audioPlayerControl = audioPlayerControl

        viewModelScope.launch {
            audioPlayerControl.getCurrentPlayStatus().collect {
                _playStatusState.value = it
            }
        }
    }

    // Функция очистки экземпляра интерфейса управления проигрывателем в сервисе
    fun removeAudioPlayerControl() {
        audioPlayerControl = null
    }
}