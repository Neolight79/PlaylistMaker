package com.example.playlistmaker.media.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.bundle.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoriteBinding
import com.example.playlistmaker.media.domain.models.FavoriteState
import com.example.playlistmaker.media.ui.view_model.FavoriteViewModel
import com.example.playlistmaker.player.ui.fragment.PlayerFragment
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteFragment : Fragment() {

    companion object {
        fun newInstance() = FavoriteFragment()
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    // Инициализируем ViewBinding
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    // Объявляем переменную для функции защиты от многократных нажатий
    private lateinit var onTrackClickDebounce: (Track) -> Unit

    // Инициализируем переменную для ViewModel
    private val favoriteViewModel: FavoriteViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Объект с методом обработки нажатий на элементы списка
    private val trackClickListener =
        object : TracksListAdapter.TrackClickListener {
            // Подключаем обработчик нажатия на элемент списка RecyclerView для списка избранных треков
            override fun onTrackClick(track: Track) {
                onTrackClickDebounce(track)
            }
            // Подключаем обработчик длинного нажатия на элемент списка RecyclerView для списка избранных треков
            override fun onTrackLongClick(track: Track) {
                onTrackClickDebounce(track)
            }
        }

    // Инициализируем адаптер для RecyclerView избранных треков
    private val favoriteListAdapter = TracksListAdapter(trackClickListener)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем функцию защиты от повторных нажатий
        onTrackClickDebounce = debounce(CLICK_DEBOUNCE_DELAY, viewLifecycleOwner.lifecycleScope, false) { track ->
            findNavController().navigate(R.id.action_mediaFragment_to_playerFragment, bundleOf(PlayerFragment.TRACK_ID to track.trackId))
        }

        // Подписываемся на получение объекта с данными LiveData
        favoriteViewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        // Готовим RecyclerView для списка избранных треков
        binding.favoriteRecyclerView.adapter = favoriteListAdapter

    }

    override fun onResume() {
        super.onResume()
        // Будем обновлять список треков каждый раз при возвращении на фрагмент со списком
        favoriteViewModel.fillData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Функция отображения состояний
    private fun render(state: FavoriteState) {
        when (state) {
            is FavoriteState.Loading -> showLoading()
            is FavoriteState.Empty -> showEmpty()
            is FavoriteState.TracksFavorite -> showTracksFavorite(state.trackList)
        }
    }

    private fun showLoading() {
        binding.favoriteRecyclerView.isVisible = false
        binding.placeholderView.isVisible = false
        binding.progressBar.isVisible = true
    }

    private fun showEmpty() {
        binding.favoriteRecyclerView.isVisible = false
        binding.placeholderView.isVisible = true
        binding.progressBar.isVisible = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showTracksFavorite(trackList: List<Track>) {

        with (binding) {
            favoriteRecyclerView.isVisible = true
            placeholderView.isVisible = false
            progressBar.isVisible = false
        }

        with (favoriteListAdapter) {
            tracksList.clear()
            tracksList.addAll(trackList)
            notifyDataSetChanged()
        }

    }

}