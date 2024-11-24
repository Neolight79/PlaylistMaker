package com.example.playlistmaker.search.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
        const val SEARCH_ADAPTER = false
        const val HISTORY_ADAPTER = true
    }

    // Переменная для ViewModel
    private val viewModel by viewModel<SearchViewModel>()

    // Переменная блокировки нажатия на кнопки (Debounce)
    private var isClickAllowed = true

    // Объект с методами обработки нажатий на элементы списка
    private val trackClickListener =
        object : SearchListAdapter.TrackClickListener {
            // Подключаем обработчик нажатия на элемент списка RecyclerView для списка найденных треков
            override fun onTrackClick(track: Track) {
                if (clickDebounce()) {
                    viewModel.addTrack(track)
                    findNavController().navigate(R.id.action_searchFragment_to_playerActivity, PlayerActivity.createArgs(track.trackId))
                }
            }

            // Подключаем обработчик нажатия на кнопку очистки истории
            override fun onClearHistoryClick() {
                viewModel.clearHistory()
                binding.searchHistoryView.isVisible = false
            }
        }

    // Инициализируем переменные для RecyclerView списка найденных треков
    private val searchListAdapter = SearchListAdapter(SEARCH_ADAPTER, trackClickListener)

    // Инициализируем переменные для RecyclerView списка истории поиска
    private val historyListAdapter = SearchListAdapter(HISTORY_ADAPTER, trackClickListener)

    // Инициализируем ручку для доступа к главному потоку
    private var mainThreadHandler = Handler(Looper.getMainLooper())

    // Переменная для TextWatcher
    private lateinit var simpleTextWatcher: TextWatcher

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем ViewModel и подписываемся на изменения состояний
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        // Устанавливаем обработчик на кнопку очистки строки поиска
        binding.clearIcon.setOnClickListener {
            binding.inputEditText.text.clear()
            viewModel.clearSearch()
        }

        // Устанавливаем обработчик на кнопку обновления после ошибки
        binding.placeholderButton.setOnClickListener {
            viewModel.searchDirectly(binding.inputEditText.text.toString())
        }

        // Реализуем механизм контроля ввода данных в строку поиска
        simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Код обработчика перед изменением текста
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearIcon.isVisible = !s.isNullOrEmpty()
                viewModel.searchDebounce(
                    changedText = s?.toString() ?: ""
                )
            }

            override fun afterTextChanged(s: Editable?) {
                // Код обработчика после изменения текста
            }
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)

        // Помимо поиска по DEBOUNCE можно запустить не дожидаясь двух секунд принудительно
        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchDirectly(binding.inputEditText.text.toString())
            }
            false
        }

        // Добавляем обработчик для смены фокуса на поле ввода
        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty() && (historyListAdapter.itemCount > 0))
                viewModel.loadHistory()
        }

        // Готовим RecyclerView для списка найденных треков
        binding.searchRecyclerView.adapter = searchListAdapter

        // Готовим RecyclerView для истории поиска
        binding.historyRecyclerView.adapter = historyListAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        simpleTextWatcher.let { binding.inputEditText.removeTextChangedListener(it) }
        _binding = null
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            mainThreadHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun render(state: SearchState) {
        when (state) {
            is SearchState.TracksFound -> showTracksFound(state.trackList)
            is SearchState.TracksHistory -> showTracksHistory(state.trackList)
            is SearchState.Empty -> showEmpty(state.errorMessage)
            is SearchState.Error -> showError(state.message)
            is SearchState.Loading -> showLoading()
        }
    }

    private fun showLoading() {
        binding.searchRecyclerView.isVisible = false
        binding.searchHistoryView.isVisible = false
        binding.placeholderView.isVisible = false
        binding.progressBar.isVisible = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showTracksFound(trackList: List<Track>) {

        with (binding) {
            searchRecyclerView.isVisible = true
            searchHistoryView.isVisible = false
            placeholderView.isVisible = false
            progressBar.isVisible = false
        }

        with (searchListAdapter) {
            foundTracks.clear()
            foundTracks.addAll(trackList)
            notifyDataSetChanged()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showTracksHistory(trackList: List<Track>) {

        with (binding) {
            searchRecyclerView.isVisible = false
            searchHistoryView.isVisible = trackList.isNotEmpty()
            placeholderView.isVisible = false
            progressBar.isVisible = false
        }

        with (historyListAdapter) {
            foundTracks.clear()
            foundTracks.addAll(trackList)
            notifyDataSetChanged()
        }

    }

    private fun showError(message: String) {
        with (binding) {
            searchRecyclerView.isVisible = false
            searchHistoryView.isVisible = false
            placeholderView.isVisible = true
            progressBar.isVisible = false

            placeholderImage.setImageResource(R.drawable.no_connection)
            placeholderButton.isVisible = true
            placeholderMessage.text = message
        }
    }

    private fun showEmpty(errorMessage: String) {
        with (binding) {
            searchRecyclerView.isVisible = false
            searchHistoryView.isVisible = false
            placeholderView.isVisible = true
            progressBar.isVisible = false

            placeholderImage.setImageResource(R.drawable.nothing_found)
            placeholderButton.isVisible = false
            placeholderMessage.text = errorMessage
        }
    }

}