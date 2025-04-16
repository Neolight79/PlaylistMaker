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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

internal class MusicService : Service() {

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_service_channel"
        const val SERVICE_NOTIFICATION_ID = 100
    }

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private val _playStatus = MutableStateFlow(PlayStatus("00:00", false))
    val playStatus = _playStatus.asStateFlow()

    // Переменная для хранения MediaPlayer
    private var mediaPlayer: MediaPlayer? = null

    private var timerJob: Job? = null

    // Глобальная переменная для хранения ссылки на трек
    private var songUrl = ""

    private val binder = MusicServiceBinder()

    override fun onBind(intent: Intent?): IBinder {
        songUrl = intent?.getStringExtra("song_url") ?: ""
        if (songUrl.isNotEmpty()) {
            mediaPlayer?.setDataSource(songUrl)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                _playStatus.value = PlayStatus("00:00", false)
            }
            mediaPlayer?.setOnCompletionListener {
                timerJob?.cancel()
                _playStatus.value = PlayStatus("00:00", false)
            }
        }
        ServiceCompat.startForeground(
            /* service = */ this,
            /* id = */ SERVICE_NOTIFICATION_ID,
            /* notification = */ createServiceNotification(),
            /* foregroundServiceType = */ ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer?.stop()
        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
        return super.onUnbind(intent)
    }

    // Инициализация ресурсов
    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_NOT_STICKY
    }

    fun playbackControl() {
        if (mediaPlayer?.isPlaying == true) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    // Запуск воспроизведения
    private fun startPlayer() {
        mediaPlayer?.start()
        _playStatus.value = PlayStatus(getCurrentPlayerPosition(), true)
        startTimer()
    }

    // Приостановка воспроизведения
    private fun pausePlayer() {
        mediaPlayer?.pause()
        timerJob?.cancel()
        _playStatus.value = PlayStatus(getCurrentPlayerPosition(), false)
    }

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

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("[Playlist Maker]")
            .setContentText("[Исполнитель - Трек]")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (mediaPlayer?.isPlaying == true) {
                delay(200L)
                _playStatus.value = PlayStatus(getCurrentPlayerPosition(), true)
            }
        }
    }

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer?.currentPosition) ?: "00:00"
    }

}