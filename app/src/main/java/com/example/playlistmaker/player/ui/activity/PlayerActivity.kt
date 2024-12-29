package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.bundle.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.player.domain.models.PlayStatus
import com.example.playlistmaker.player.domain.models.PlayerState
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val TRACK_ID = "TRACK_ID"
        fun createArgs(trackId: Int): Bundle = bundleOf(TRACK_ID to trackId)
    }

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel by viewModel<PlayerViewModel> { parametersOf(intent.getIntExtra(TRACK_ID, 0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем тулбар для реализации возврата в корневую активити
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

        binding.favoritButton.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        // Подписываемся на получение состояний экрана
        viewModel.observeScreenState().observe(this) { screenState ->
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

                    // Присваиваем значения элементам экрана из данных состояния
                    with(binding) {
                        trackName.text =
                            screenState.trackModel.trackName
                        artistName.text =
                            screenState.trackModel.artistName
                        durationDetailsData.text =
                            screenState.trackModel.trackTimeString
                        albumDetailsData.text =
                            checkNullForDetails(screenState.trackModel.collectionName, albumGroup)
                        yearDetailsData.text =
                            checkNullForDetails(screenState.trackModel.releaseDate, yearGroup)
                        genreDetailsData.text =
                            checkNullForDetails(screenState.trackModel.primaryGenreName, genreGroup)
                        countryDetailsData.text =
                            checkNullForDetails(screenState.trackModel.country, countryGroup)
                    }
                    setFavorite(screenState.trackModel.isFavorite)
                }
                is PlayerState.FavoriteMark -> {
                    setFavorite(screenState.isFavorite)
                }
            }
        }

        // Подписываемся на получение состояний проигрывателя
        viewModel.observePlayStatus().observe(this) { playStatus ->
            changeButtonStyle(playStatus)
            refreshPlayingTime(playStatus)
        }

        // Настраиваем системное ограничение для элементов активити -
        // элементы не должны залезать под системные
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        with(binding) {
            if (loading) {
                // Статус "Загрузка"
                // Показываем
                progressBar.isVisible = true
                // Скрываем
                titleBlockGroup.isVisible = false
                playerBlockGroup.isVisible = false
                detailsBlockGroup.isVisible = false
                placeholderView.isVisible = false
            } else if (errorMessage.isNullOrEmpty()) {
                // Статус "Отображение контента"
                // Показываем
                titleBlockGroup.isVisible = true
                playerBlockGroup.isVisible = true
                detailsBlockGroup.isVisible = true
                // Скрываем
                progressBar.isVisible = false
                placeholderView.isVisible = false
            } else {
                // Статус "Ошибка"
                // Показываем
                placeholderMessage.text = errorMessage
                placeholderView.isVisible = true
                // Скрываем
                progressBar.isVisible = false
                titleBlockGroup.isVisible = false
                playerBlockGroup.isVisible = false
                detailsBlockGroup.isVisible = false
            }
        }
    }

    private fun checkNullForDetails(text: String?, groupView: View): String {
        groupView.isVisible = !text.isNullOrEmpty()
        return if (text.isNullOrEmpty()) "" else text
    }

    private fun setFavorite(isFavorite: Boolean) {
        when (isFavorite) {
            true -> binding.favoritButton.setImageResource(R.drawable.favoritbutton_glif_enabled)
            false -> binding.favoritButton.setImageResource(R.drawable.favoritbutton_glif)
        }
    }

}