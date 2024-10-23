package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.player.domain.models.PlayStatus
import com.example.playlistmaker.player.domain.models.PlayerState

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val TRACK_ID = "TRACK_ID"
    }

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel by viewModels<PlayerViewModel> { PlayerViewModel.getViewModelFactory(intent.getIntExtra(
        TRACK_ID, 0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.playButton.setOnClickListener {
            viewModel.playbackControl()
        }

        binding.placeholderButton.setOnClickListener {
            viewModel.loadTrackData()
        }

        // Подписываемся на получение состояний экрана
        viewModel.getScreenStateLiveData().observe(this) { screenState ->
            when (screenState) {
                is PlayerState.Loading -> {
                    changeContentVisibility(loading = true, errorMessage = null)
                }
                is PlayerState.Error -> {
                    changeContentVisibility(loading = false, errorMessage = screenState.message)
                }
                is PlayerState.Empty -> {
                    changeContentVisibility(loading = false, errorMessage = screenState.errorMessage)
                }
                is PlayerState.Content -> {
                    changeContentVisibility(loading = false, errorMessage = null)

                    // Наполняем активити данными трека
                    Glide.with(this)
                        .load(screenState.trackModel.artworkUrl512)
                        .transform(
                            CenterCrop(), RoundedCorners(
                                TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    8F, this.resources.displayMetrics).toInt())
                        )
                        .placeholder(R.drawable.placeholder)
                        .into(binding.artwork)

                    binding.trackName.text = screenState.trackModel.trackName
                    binding.artistName.text = screenState.trackModel.artistName

                    // Присваиваем значения деталей трека
                    binding.durationDetailsData.text = screenState.trackModel.trackTimeString
                    binding.albumDetailsData.text = checkNullForDetails(screenState.trackModel.collectionName, binding.albumGroup)
                    binding.yearDetailsData.text = checkNullForDetails(screenState.trackModel.releaseDate, binding.yearGroup)
                    binding.genreDetailsData.text = checkNullForDetails(screenState.trackModel.primaryGenreName, binding.genreGroup)
                    binding.countryDetailsData.text = checkNullForDetails(screenState.trackModel.country, binding.countryGroup)
                }
            }
        }

        // Подписываемся на получение состояний проигрывателя
        viewModel.getPlayStatusLiveData().observe(this) { playerStatus ->
            changeButtonStyle(playerStatus)
            refreshPlayingTime(playerStatus)
        }

    }

    private fun refreshPlayingTime(playStatus: PlayStatus) {
        binding.playTime.text = playStatus.currentPosition
    }

    private fun changeButtonStyle(playStatus: PlayStatus) {
        when (playStatus.isPlaying) {
            true -> binding.playButton.setImageResource(R.drawable.pausebutton_glif)
            false -> binding.playButton.setImageResource(R.drawable.playbutton_glif)
        }
    }

    private fun changeContentVisibility(loading: Boolean, errorMessage: String?) {
        if (loading) {
            // Статус "Загрузка"
            // Показываем
            binding.progressBar.isVisible = true
            // Скрываем
            binding.titleBlockGroup.isVisible = false
            binding.playerBlockGroup.isVisible = false
            binding.detailsBlockGroup.isVisible = false
            binding.placeholderView.isVisible = false
        } else if (errorMessage.isNullOrEmpty()) {
            // Статус "Отображение контента"
            // Показываем
            binding.titleBlockGroup.isVisible = true
            binding.playerBlockGroup.isVisible = true
            binding.detailsBlockGroup.isVisible = true
            // Скрываем
            binding.progressBar.isVisible = false
            binding.placeholderView.isVisible = false
        } else {
            // Статус "Ошибка"
            // Показываем
            binding.placeholderMessage.text = errorMessage
            binding.placeholderView.isVisible = true
            // Скрываем
            binding.progressBar.isVisible = false
            binding.titleBlockGroup.isVisible = false
            binding.playerBlockGroup.isVisible = false
            binding.detailsBlockGroup.isVisible = false
        }
    }

    private fun checkNullForDetails(text: String?, groupView: View): String {
        groupView.isVisible = !text.isNullOrEmpty()
        return if (text.isNullOrEmpty()) "" else text
    }

}