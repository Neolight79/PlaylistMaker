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
import com.example.playlistmaker.player.domain.models.PlayStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class PlayerViewModel(
    private val trackId: Int,
    private val tracksInteractor: TracksInteractor,
    private val mediaPlayer: MediaPlayer,
    application: Application): AndroidViewModel(application) {

    companion object {
        // Статусы для проигрывателя
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val REFRESH_TIMER_DELAY_MILLIS = 300L
    }

    // Переменная для LiveData статусов экрана проигрывателя
    private var screenStateLiveData = MutableLiveData<PlayerState>(PlayerState.Loading)
    fun observeScreenState(): MutableLiveData<PlayerState> = screenStateLiveData

    // Переменная для LiveData статуса проигрывания трека
    private var playStatusLiveData = MutableLiveData<PlayStatus>()
    fun observePlayStatus(): MutableLiveData<PlayStatus> = playStatusLiveData

    // Переменная хранения текущего состояния проигрывателя
    private var playState = STATE_DEFAULT

    // Инициализируем job для таймера
    private var timerJob: Job? = null

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

    // Функция очистки перед закрытием
    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }

    // Функция отправки статуса экрана на View
    private fun renderState(state: PlayerState) {
        screenStateLiveData.postValue(state)
    }

    private fun getCurrentPlayStatus(): PlayStatus {
        return playStatusLiveData.value ?: PlayStatus(currentPosition = getApplication<Application>().resources.getString(R.string.zero_duration), isPlaying = false)
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