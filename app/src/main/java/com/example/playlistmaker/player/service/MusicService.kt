package com.example.playlistmaker.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.player.domain.models.PlayStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

internal class MusicService : Service(), AudioPlayerControl {

    // region Переменные класса

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_service_channel"
        const val SERVICE_NOTIFICATION_ID = 100
        const val REFRESH_TIMER_PERIOD = 200L
    }

    // Объявляем внутренний класс для передачи экземпляра сервиса
    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    // Переменные для передачи статуса проигрывателя через StateFlow
    private val _playStatus = MutableStateFlow(PlayStatus())
    private val playStatus = _playStatus.asStateFlow()

    // Переменная для экземпляра MediaPlayer
    private var mediaPlayer: MediaPlayer? = null

    // Переменная для обновления статуса проигрывателя по таймеру
    private var timerJob: Job? = null

    // Экземпляр внутреннего класса для передачи экземпляра сервиса
    private val binder = MusicServiceBinder()

    // Переменные для хранения заголовка и текста уведомления
    private var notificationTitle = ""
    private var notificationText = ""

    // endregion

    // region Переопределяемые функции сервиса

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    // Привязка сервиса
    override fun onBind(intent: Intent?): IBinder {

        // Получаем из интента строки для отображения в уведомлении и создаём уведомление
        notificationTitle = intent?.getStringExtra("notification_title") ?: ""
        notificationText = intent?.getStringExtra("notification_text") ?: ""

        // Инициализируем проигрыватель, если получили URL трека
        val songUrl = intent?.getStringExtra("song_url") ?: ""

        if (songUrl.isNotEmpty()) {
            initMediaPlayer(songUrl)
        }

        return binder

    }

    // Отвязывание сервиса
    override fun onUnbind(intent: Intent?): Boolean {
        // Останавливаем таймер
        stopTimer()
        // Освобождаем проигрыватель
        releaseMediaPlayer()
        return super.onUnbind(intent)
    }

    // endregion

    // region Переопределяемые функции интерфейса AudioPlayerControl

    // Получение текущего статуса воспроизведения
    override fun getCurrentPlayStatus(): StateFlow<PlayStatus> {
        return playStatus
    }

    // Запуск воспроизведения
    override fun startPlayer() {
        mediaPlayer?.start()
        _playStatus.value = PlayStatus(getCurrentPlayerPosition(), true)
        startTimer()
    }

    // Приостановка воспроизведения
    override fun pausePlayer() {
        mediaPlayer?.pause()
        _playStatus.value = PlayStatus(getCurrentPlayerPosition(), false)
        stopTimer()
    }

    override fun startForeground() {
        ServiceCompat.startForeground(
            /* service = */ this,
            /* id = */ SERVICE_NOTIFICATION_ID,
            /* notification = */ createServiceNotification(),
            /* foregroundServiceType = */ ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    override fun stopForeground() {
        ServiceCompat.stopForeground(
            /* service = */ this,
            /* flags = */ ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    // endregion

    // region Приватные функции

    // Создание канала уведомлений
    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            /* id= */ NOTIFICATION_CHANNEL_ID,
            /* name= */ "Music service",
            /* importance= */ NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Service for playing music"

        // Регистрируем канал уведомлений
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Создание уведомления для foreground сервиса
    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    // Инициализируем проигрыватель
    private fun initMediaPlayer(songUrl: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(songUrl)
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            _playStatus.value = PlayStatus()
        }
        mediaPlayer?.setOnCompletionListener {
            stopTimer()
            stopForeground()
            _playStatus.value = PlayStatus()
        }
    }

    // Освобождаем проигрыватель
    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Запуск таймера отслеживания времени проигрывания трека
    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (mediaPlayer?.isPlaying == true) {
                delay(REFRESH_TIMER_PERIOD)
                _playStatus.value = PlayStatus(getCurrentPlayerPosition(), true)
            }
        }
    }

    // Остановка таймера отслеживания времени проигрывания трека
    private fun stopTimer() {
        timerJob?.cancel()
    }

    // Получение строки с текущим временем проигрывания трека
    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer?.currentPosition) ?: getString(R.string.zero_duration)
    }

    // endregion

}