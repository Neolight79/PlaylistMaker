package com.example.playlistmaker.player.ui.view_model

import android.app.Application
import androidx.lifecycle.MutableLiveData
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val trackId: Int,
    private val tracksInteractor: TracksInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: PlaylistsInteractor,
    private val application: Application): ViewModel() {

    // Переменная для LiveData состояния элементов экрана проигрывателя
    private val screenStateLiveData = MutableLiveData<PlayerState>(PlayerState.Loading)
    fun observeScreenState(): MutableLiveData<PlayerState> = screenStateLiveData

    // Переменная для LiveData текущего статуса проигрывания трека
    private val playStatusLiveData = MutableLiveData<PlayStatus>()
    fun observePlayStatus(): MutableLiveData<PlayStatus> = playStatusLiveData

    // Переменная для хранения экземпляра интерфейса взаимодействия с сервисом проигрывателя
    private var audioPlayerControl: AudioPlayerControl? = null

    // Переменная для LiveData признака избранного трека
    private val isFavoriteLiveData = MutableLiveData<Boolean>()
    fun observeIsFavorite(): MutableLiveData<Boolean> = isFavoriteLiveData

    // Переменная для LiveData состояния показа BottomSheet
    private val bottomSheetStateLiveData = MutableLiveData<BottomSheetState>()
    fun observeBottomSheetState(): MutableLiveData<BottomSheetState> = bottomSheetStateLiveData

    // Переменная для LiveData списка плейлистов
    private val playlistsLiveData = MutableLiveData<List<Playlist>>()
    fun observePlaylists(): MutableLiveData<List<Playlist>> = playlistsLiveData

    // Переменная для хранения объекта трека для работы с избранным
    private lateinit var currentTrack: Track

    init {
        // Убираем BottomSheet
        onBottomSheetChangedState(BottomSheetBehavior.STATE_HIDDEN)
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
                        trackModel = foundTracks[0]
                    )
                )
            }
        }
    }

    // Функция переключения режима проигрывателя
    fun playbackControl() {
        if (playStatusLiveData.value?.isPlaying == true) {
            audioPlayerControl?.pausePlayer()
        } else {
            audioPlayerControl?.startPlayer()
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
        currentTrack.apply { isFavorite = !isFavorite }
        // Отправляем на фрагмент
        renderFavorite(currentTrack.isFavorite)
    }

    // Обработка нажатия на кнопку добавления трека в плейлист
    fun onAddToPlaylistClicked() {
        // Показываем BottomSheet
        onBottomSheetChangedState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    // Обработка нажатия на плейлист для добавления трека
    fun onPlaylistClicked(playlist: Playlist, bottomSheetState: Int) {

        // Проверяем наличие трека в текущем плейлисте и добавляем его, если его там еще нет
        if (playlist.playlistTracks.contains(currentTrack.trackId)) {
            bottomSheetStateLiveData.postValue(BottomSheetState(bottomSheetState,
                application.getString(R.string.exists_in_playlist_message,
                    playlist.playlistName)))
        } else {
            viewModelScope.launch {
                playlistsInteractor.addTrackToPlaylist(currentTrack, playlist)
            }
            bottomSheetStateLiveData.postValue(BottomSheetState(BottomSheetBehavior.STATE_HIDDEN,
                application.getString(R.string.added_to_playlist_message,
                    playlist.playlistName)))
            refillPlaylists()
        }

    }

    // Обработка изменения состояние BottomSheet
    fun onBottomSheetChangedState(newState: Int) {
        bottomSheetStateLiveData.postValue(BottomSheetState(newState, null))
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
        screenStateLiveData.postValue(state)
    }

    // Функция отправки значения для признака нахождения трека в избранных
    private fun renderFavorite(isFavorite: Boolean) {
        isFavoriteLiveData.postValue(isFavorite)
    }

    // Функция отправки списка плейлистов
    private fun renderPlaylists(playlists: List<Playlist>) {
        playlistsLiveData.postValue(playlists)
    }

    // Привязка экземпляра интерфейса для управления проигрывателем в сервисе
    fun setAudioPlayerControl(audioPlayerControl: AudioPlayerControl) {
        this.audioPlayerControl = audioPlayerControl

        viewModelScope.launch {
            audioPlayerControl.getCurrentPlayStatus().collect {
                playStatusLiveData.postValue(it)
            }
        }
    }

    // Функция очистки экземпляра интерфейса управления проигрывателем в сервисе
    fun removeAudioPlayerControl() {
        audioPlayerControl = null
    }

}