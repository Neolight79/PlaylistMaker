package com.example.playlistmaker.player.ui.view_model

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.search.domain.api.TracksInteractor
import com.example.playlistmaker.search.domain.models.Track
import android.icu.text.SimpleDateFormat
import com.example.playlistmaker.player.domain.models.PlayStatus
import java.util.Locale

class PlayerViewModel(
    private val trackId: Int,
    application: Application): AndroidViewModel(application) {

    companion object {
        // Статусы для проигрывателя
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val REFRESH_TIMER_DELAY_MILLIS = 500L

        // Инициализация ViewModel
        fun getViewModelFactory(trackId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(
                    trackId,
                    (this[APPLICATION_KEY] as Application)
                )
            }
        }
    }

    // Переменная для LiveData статусов экрана проигрывателя
    private var screenStateLiveData = MutableLiveData<PlayerState>(PlayerState.Loading)

    // Переменная для LiveData статуса проигрывания трека
    private var playStatusLiveData = MutableLiveData<PlayStatus>()

    // Переменная для интерактора получения данных трека
    private val tracksInteractor = Creator.provideTracksInteractor(getApplication())

    // Переменная объекта проигрывателя
    private var mediaPlayer = MediaPlayer()

    // Переменная хранения текущего состояния проигрывателя
    private var playerState = STATE_DEFAULT

    // Инициализируем ручку для доступа к главному потоку
    private var mainThreadHandler: Handler? = null

    init {

        // Создаём handler для доступа из дополнительных потоков к главному
        mainThreadHandler = Handler(Looper.getMainLooper())

        // Загружаем данные трека из сети
        loadTrackData()

    }

    // Функция загрузки данных трека из сети через интерактор
    fun loadTrackData() {
        tracksInteractor.loadTrackData(
            trackId = trackId, object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
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
            }
        )
    }

    // Функция возвращающая объект LiveData состояния экрана проигрывателя
    fun getScreenStateLiveData(): LiveData<PlayerState> = screenStateLiveData

    // Функция возвращающая объект LiveData состояния экрана проигрывателя
    fun getPlayStatusLiveData(): LiveData<PlayStatus> = playStatusLiveData

    // Функция подготовки проигрывателя к работе
    private fun preparePlayer(trackPreviewUrl: String) {
        mediaPlayer.setDataSource(trackPreviewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerState = STATE_PREPARED
            refreshPlayingState()
        }
        mediaPlayer.setOnCompletionListener {
            playerState = STATE_PREPARED
            refreshPlayingState()
        }
    }

    // Функция запуска проигрывателя
    private fun play() {

        mediaPlayer.start()
        playerState = STATE_PLAYING

        // Отправляем на View статус начала проигрывания трека
        refreshPlayingState()

        // Запускаем механизм обновления данных счётчика проигрывателя
        // Запускаем поток, который будет обновлять время
        mainThreadHandler?.postDelayed(
            object : Runnable {
                override fun run() {
                    // Обновляем время в главном потоке
                    refreshPlayingState()

                    // И снова планируем то же действие
                    if (playerState == STATE_PLAYING)
                        mainThreadHandler?.postDelayed(
                            this,
                            REFRESH_TIMER_DELAY_MILLIS
                        )
                }
            },
            REFRESH_TIMER_DELAY_MILLIS
        )

    }

    // Функция приостановки проигрывателя
    private fun pause() {
        mainThreadHandler?.removeCallbacksAndMessages(null)
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        refreshPlayingState()
    }

    // Функция переключения режима проигрывателя
    fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> pause()
            STATE_PREPARED, STATE_PAUSED -> play()
        }
    }

    // Функция очистки перед закрытием
    override fun onCleared() {
        mediaPlayer.release()
        mainThreadHandler?.removeCallbacksAndMessages(null)
    }

    // Функция отправки статуса экрана на View
    private fun renderState(state: PlayerState) {
        screenStateLiveData.postValue(state)
    }

    private fun getCurrentPlayStatus(): PlayStatus {
        return playStatusLiveData.value ?: PlayStatus(currentPosition = getApplication<Application>().resources.getString(R.string.zero_duration), isPlaying = false)
    }

    private fun refreshPlayingState() {
        when(playerState) {
            STATE_PLAYING ->
                playStatusLiveData.value = getCurrentPlayStatus().copy(currentPosition = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition), isPlaying = true)
            STATE_PAUSED ->
                playStatusLiveData.value = getCurrentPlayStatus().copy(currentPosition = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition), isPlaying = false)
            STATE_PREPARED ->
                playStatusLiveData.value = getCurrentPlayStatus().copy(currentPosition = getApplication<Application>().resources.getString(R.string.zero_duration), isPlaying = false)
        }
    }

}