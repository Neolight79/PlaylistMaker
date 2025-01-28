package com.example.playlistmaker.player.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.ui.fragment.BottomSheetPlaylistsAdapter
import com.example.playlistmaker.player.domain.models.PlayStatus
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment : Fragment() {

    companion object {
        const val TRACK_ID = "TRACK_ID"
    }

    // Инициализируем ViewBinding
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val viewModel by viewModel<PlayerViewModel> {
        parametersOf(
            requireArguments().getInt(
                TRACK_ID, 0
            )
        )
    }

    private var isCreatePlaylistCalled: Boolean = false

    // Объект с методом обработки нажатий на элементы списка плейлистов
    private val onPlaylistClick: (Playlist) -> Unit = { playlist ->
        viewModel.onPlaylistClicked(playlist, bottomSheetBehavior.state)
    }

    // Инициализируем адаптер для RecyclerView для списка плейлистов
    private val playlistsAdapter = BottomSheetPlaylistsAdapter(onPlaylistClick)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подключаем BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)

        // Готовим RecyclerView для списка плейлистов
        binding.playlistsRecyclerView.adapter = playlistsAdapter

        // Обработчик нажатия на кнопку Назад
        activity?.onBackPressedDispatcher?.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        // Подключаем обработчик нажатия на кнопку назад
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
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

        binding.plusButton.setOnClickListener {
            viewModel.onAddToPlaylistClicked()
        }

        // Обработчик нажатия на кнопку создания плейлиста
        binding.createPlaylist.setOnClickListener {
            isCreatePlaylistCalled = true
            viewModel.onBottomSheetChangedState(BottomSheetBehavior.STATE_HIDDEN)
        }

        // Подписываемся на получение состояний экрана
        viewModel.observeScreenState().observe(viewLifecycleOwner) { screenState ->
            when (screenState) {
                is PlayerState.Loading -> {
                    changeContentVisibility(loading = true, errorMessage = null)
                }

                is PlayerState.Error -> {
                    changeContentVisibility(loading = false, errorMessage = screenState.message)
                }

                is PlayerState.Empty -> {
                    changeContentVisibility(
                        loading = false,
                        errorMessage = screenState.errorMessage
                    )
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
                                    8F, this.resources.displayMetrics
                                ).toInt()
                            )
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
            }
        }

        // Подписываемся на получение состояний проигрывателя
        viewModel.observePlayStatus().observe(viewLifecycleOwner) { playStatus ->
            changeButtonStyle(playStatus)
            refreshPlayingTime(playStatus)
        }

        // Подписываемся на получение признака избранного трека
        viewModel.observeIsFavorite().observe(viewLifecycleOwner) { isFavorite ->
            setFavorite(isFavorite)
        }

        // Подписываемся на получение списка плейлистов
        viewModel.observePlaylists().observe(viewLifecycleOwner) { newPlaylists ->
            // Заполняем RecyclerView плейлистами
            with(playlistsAdapter) {
                playlists.clear()
                playlists.addAll(newPlaylists)
                notifyDataSetChanged()
            }
        }

        // Подписываемся на получение состояния BottomSheet
        viewModel.observeBottomSheetState().observe(viewLifecycleOwner) { newState ->
            if (newState.newState != bottomSheetBehavior.state) {
                bottomSheetBehavior.state = newState.newState
            }
            changeBottomSheetVisibility(newState.newState)
            if (!newState.message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), newState.message, Toast.LENGTH_LONG).show()
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val isObservableState =
                    newState == BottomSheetBehavior.STATE_HIDDEN ||
                        newState == BottomSheetBehavior.STATE_COLLAPSED ||
                        newState == BottomSheetBehavior.STATE_EXPANDED
                if (isObservableState) {
                    viewModel.onBottomSheetChangedState(newState)
                    changeBottomSheetVisibility(newState)
                }
                if (isCreatePlaylistCalled && newState == BottomSheetBehavior.STATE_HIDDEN) {
                    isCreatePlaylistCalled = false
                    findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = (slideOffset + 1) / 2
            }
        })
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

    private fun changeBottomSheetVisibility(state: Int) {
        if (state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.standardBottomSheet.isVisible = false
            binding.overlay.isVisible = false
        } else {
            binding.standardBottomSheet.isVisible = true
            binding.overlay.isVisible = true
        }
    }

    private fun changeContentVisibility(loading: Boolean, errorMessage: String?) {
        with(binding) {
            if (loading) {
                // Статус "Загрузка"
                // Показываем
                progressBar.isVisible = true
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

    override fun onResume() {
        super.onResume()
        viewModel.refillPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}