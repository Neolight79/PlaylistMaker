@file:Suppress("DEPRECATION")

package com.example.playlistmaker

import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track

    private var mediaPlayer = MediaPlayer()

    private var playerState = STATE_DEFAULT

    // Инициализируем ручку для доступа к главному потоку
    private var mainThreadHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        // Создаём handler для доступа из дополнительных потоков к главному
        mainThreadHandler = Handler(Looper.getMainLooper())

        // Восстанавливаем данные трека из Json
        track = intent.getSerializableExtra(TRACK) as Track

        // Наполняем активити данными объекта
        Glide.with(this)
            .load(track.artworkUrl512)
            .transform(
                CenterCrop(), RoundedCorners(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                8F, this.resources.displayMetrics).toInt())
            )
            .placeholder(R.drawable.placeholder)
            .into(binding.artwork)

        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName

        // Присваиваем значения деталей трека
        binding.durationDetailsData.text = track.trackTimeString
        binding.albumDetailsData.text = checkNullForDetails(track.collectionName, binding.albumGroup)
        val releaseDate = checkNullForDetails(track.releaseDate, binding.yearGroup)
        binding.yearDetailsData.text = if (releaseDate.isEmpty()) "" else releaseDate.substring(0, 4)
        binding.genreDetailsData.text = checkNullForDetails(track.primaryGenreName, binding.genreGroup)
        binding.countryDetailsData.text = checkNullForDetails(track.country, binding.countryGroup)

        // Готовим проигрыватель к проигрыванию
        preparePlayer()

        binding.playButton.setOnClickListener {
            playbackControl()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        mainThreadHandler?.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mainThreadHandler?.removeCallbacksAndMessages(null)
    }

    private fun checkNullForDetails(text: String?, groupView: View): String {
        groupView.isVisible = !text.isNullOrEmpty()
        return if (text.isNullOrEmpty()) "" else text
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(track.previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerState = STATE_PREPARED
            refreshPlayingTime()
        }
        mediaPlayer.setOnCompletionListener {
            binding.playButton.setImageResource(R.drawable.playbutton_glif)
            playerState = STATE_PREPARED
            refreshPlayingTime()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        binding.playButton.setImageResource(R.drawable.pausebutton_glif)
        playerState = STATE_PLAYING

        // Запускаем механизм обновления данных счётчика проигрывателя
        // Запускаем поток, который будет обновлять время
        mainThreadHandler?.postDelayed(
            object : Runnable {
                override fun run() {
                    // Обновляем время в главном потоке
                    refreshPlayingTime()

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

    private fun pausePlayer() {
        mediaPlayer.pause()
        binding.playButton.setImageResource(R.drawable.playbutton_glif)
        playerState = STATE_PAUSED
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun refreshPlayingTime() {
        when(playerState) {
            STATE_PLAYING -> {
                binding.playTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
            }
            STATE_PREPARED -> {
                binding.playTime.text = getString(R.string.zero_duration)
            }
        }
    }

    companion object {
        private const val TRACK = "TRACK"
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val REFRESH_TIMER_DELAY_MILLIS = 500L
    }
}