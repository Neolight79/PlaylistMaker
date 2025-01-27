package com.example.playlistmaker.media.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.domain.models.PlaylistsState
import com.example.playlistmaker.media.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.util.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    companion object {
        fun newInstance() = PlaylistsFragment()
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    // Инициализируем ViewBinding
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    // Объявляем переменную для функции защиты от многократных нажатий
    private lateinit var onPlaylistClickDebounce: (Playlist) -> Unit

    // Инициализируем переменную для ViewModel
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Объект с методом обработки нажатий на элементы списка
    private val onPlaylistClick: (Playlist) -> Unit = { playlist ->
                onPlaylistClickDebounce(playlist)
    }

    // Инициализируем адаптер для RecyclerView плейлистов
    private val playlistsAdapter = PlaylistsAdapter(onPlaylistClick)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем функцию защиты от повторных нажатий
        onPlaylistClickDebounce = debounce(CLICK_DEBOUNCE_DELAY, viewLifecycleOwner.lifecycleScope, false) { playlist ->
            playlistsViewModel.openPlaylist(playlist)
        }

        // Подписываемся на получение объекта с данными LiveData
        playlistsViewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        // Готовим RecyclerView для списка плейлистов
        binding.playlistsRecyclerView.adapter = playlistsAdapter

        // Обработчик нажатия на кнопку добавления плейлиста
        binding.createPlaylist.setOnClickListener {
                findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        // Будем обновлять список треков каждый раз при возвращении на фрагмент со списком
        playlistsViewModel.fillData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Функция отображения состояний
    private fun render(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Loading -> showLoading()
            is PlaylistsState.Empty -> showEmpty()
            is PlaylistsState.Playlists -> showPlaylists(state.playlists)
        }
    }

    private fun showLoading() {
        binding.playlistsRecyclerView.isVisible = false
        binding.placeholderView.isVisible = false
        binding.progressBar.isVisible = true
    }

    private fun showEmpty() {
        binding.playlistsRecyclerView.isVisible = false
        binding.placeholderView.isVisible = true
        binding.progressBar.isVisible = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showPlaylists(playlists: List<Playlist>) {

        with (binding) {
            playlistsRecyclerView.isVisible = true
            placeholderView.isVisible = false
            progressBar.isVisible = false
        }

            playlistsAdapter.playlists.clear()
            playlistsAdapter.playlists.addAll(playlists)
            playlistsAdapter.notifyDataSetChanged()

    }

}