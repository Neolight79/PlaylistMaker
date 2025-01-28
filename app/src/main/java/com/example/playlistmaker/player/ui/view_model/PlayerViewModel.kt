package com.example.playlistmaker.player.ui.view_model

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.playlistmaker.R
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.Track
import android.icu.text.SimpleDateFormat
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.db.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.BottomSheetState
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.player.domain.models.PlayStatus
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class PlayerViewModel(
    private val trackId: Int,
    private val tracksInteractor: TracksInteractor,
    private val mediaPlayer: MediaPlayer,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: PlaylistsInteractor,
    application: Application): AndroidViewModel(application) {

    companion object {
        // Статусы для проигрывателя
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val REFRESH_TIMER_DELAY_MILLIS = 300L
    }

    // Переменная для LiveData состояния элементов экрана проигрывателя
    private val screenStateLiveData = MutableLiveData<PlayerState>(PlayerState.Loading)
    fun observeScreenState(): MutableLiveData<PlayerState> = screenStateLiveData

    // Переменная для LiveData текущего статуса проигрывания трека
    private val playStatusLiveData = MutableLiveData<PlayStatus>()
    fun observePlayStatus(): MutableLiveData<PlayStatus> = playStatusLiveData

    // Переменная для LiveData признака избранного трека
    private val isFavoriteLiveData = MutableLiveData<Boolean>()
    fun observeIsFavorite(): MutableLiveData<Boolean> = isFavoriteLiveData

    // Переменная для LiveData состояния показа BottomSheet
    private val bottomSheetStateLiveData = MutableLiveData<BottomSheetState>()
    fun observeBottomSheetState(): MutableLiveData<BottomSheetState> = bottomSheetStateLiveData

    // Переменная для LiveData списка плейлистов
    private val playlistsLiveData = MutableLiveData<List<Playlist>>()
    fun observePlaylists(): MutableLiveData<List<Playlist>> = playlistsLiveData

    // Переменная хранения текущего состояния проигрывателя
    private var playState = STATE_DEFAULT

    // Инициализируем job для таймера
    private var timerJob: Job? = null

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
                        message = getApplication<Application>().getString(R.string.errorCommon),
                    )
                )
            }

            foundTracks.isNullOrEmpty() -> {
                renderState(
                    PlayerState.Empty(
                        errorMessage = getApplication<Application>().getString(R.string.nothing_found),
                    )
                )
            }

            else -> {
                currentTrack = foundTracks[0]
                preparePlayer(foundTracks[0].previewUrl)
                renderState(
                    PlayerState.Content(
                        trackModel = foundTracks[0]
                    )
                )
            }
        }
    }

    // Функция подготовки проигрывателя к работе
    private fun preparePlayer(trackPreviewUrl: String) {
        mediaPlayer.setDataSource(trackPreviewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playState = STATE_PREPARED
            refreshPlayingState()
        }
        mediaPlayer.setOnCompletionListener {
            playState = STATE_PREPARED
            refreshPlayingState()
        }
    }

    // Функция запуска проигрывателя
    private fun play() {

        mediaPlayer.start()
        playState = STATE_PLAYING

        // Отправляем на View статус начала проигрывания трека
        refreshPlayingState()

        // Запускаем механизм обновления данных счётчика проигрывателя
        // Запускаем поток, который будет обновлять время
        startTimer()

    }

    // Функция приостановки проигрывателя
    private fun pause() {
        timerJob?.cancel()
        mediaPlayer.pause()
        playState = STATE_PAUSED
        refreshPlayingState()
    }

    // Функция переключения режима проигрывателя
    fun playbackControl() {
        when(playState) {
            STATE_PLAYING -> pause()
            STATE_PREPARED, STATE_PAUSED -> play()
        }
    }

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

    fun onAddToPlaylistClicked() {
        // Показываем BottomSheet
        onBottomSheetChangedState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    fun onPlaylistClicked(playlist: Playlist, bottomSheetState: Int) {

        // Проверяем наличие трека в текущем плейлисте и добавляем его, если его там еще нет
        if (playlist.playlistTracks.contains(currentTrack.trackId)) {
            bottomSheetStateLiveData.postValue(BottomSheetState(bottomSheetState,
                getApplication<Application>().resources.getString(R.string.exists_in_playlist_message,
                    playlist.playlistName)))
        } else {
            viewModelScope.launch {
                playlistsInteractor.addTrackToPlaylist(currentTrack, playlist)
            }
            bottomSheetStateLiveData.postValue(BottomSheetState(BottomSheetBehavior.STATE_HIDDEN,
                getApplication<Application>().resources.getString(R.string.added_to_playlist_message,
                    playlist.playlistName)))
            refillPlaylists()
        }

    }

    fun onBottomSheetChangedState(newState: Int) {
        bottomSheetStateLiveData.postValue(BottomSheetState(newState, null))
    }

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
        mediaPlayer.release()
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

    private fun getCurrentPlayStatus(): PlayStatus {
        return playStatusLiveData.value ?: PlayStatus(currentPosition = getApplication<Application>()
            .resources.getString(R.string.zero_duration), isPlaying = false)
    }

    private fun refreshPlayingState() {
        when(playState) {
            STATE_PLAYING ->
                playStatusLiveData.value = getCurrentPlayStatus()
                    .copy(
                        currentPosition = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition),
                        isPlaying = true)
            STATE_PAUSED ->
                playStatusLiveData.value = getCurrentPlayStatus()
                    .copy(
                        currentPosition = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition),
                        isPlaying = false)
            STATE_PREPARED ->
                playStatusLiveData.value = getCurrentPlayStatus()
                    .copy(
                        currentPosition = getApplication<Application>().resources.getString(R.string.zero_duration),
                        isPlaying = false)
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                delay(REFRESH_TIMER_DELAY_MILLIS)
                refreshPlayingState()
            }
        }
    }

}