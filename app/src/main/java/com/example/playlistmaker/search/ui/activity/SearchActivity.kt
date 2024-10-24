package com.example.playlistmaker.search.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import com.example.playlistmaker.search.domain.models.SearchState

class SearchActivity : AppCompatActivity() {

    companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    // Переменная для ViewBinding
    private lateinit var binding: ActivitySearchBinding

    // Переменная для ViewModel
    private lateinit var viewModel: SearchViewModel

    // Переменная блокировки нажатия на кнопки (Debounce)
    private var isClickAllowed = true

    // Объект с методами обработки нажатий на элементы списка
    private val trackClickListener =
        object : SearchListAdapter.TrackClickListener {
        // Подключаем обработчик нажатия на элемент списка RecyclerView для списка найденных треков
        override fun onTrackClick(track: Track) {
            if (clickDebounce()) {
                viewModel.addTrack(track)
                val playerIntent = Intent(this@SearchActivity, PlayerActivity::class.java)
                playerIntent.putExtra("TRACK_ID", track.trackId)
                startActivity(playerIntent)
            }
        }

        // Подключаем обработчик нажатия на кнопку очистки истории
        override fun onClearHistoryClick() {
            viewModel.clearHistory()
            binding.searchHistoryView.isVisible = false
        }
    }

    // Инициализируем переменные для RecyclerView списка найденных треков
    private val searchListAdapter = SearchListAdapter(false, trackClickListener)

    // Инициализируем переменные для RecyclerView списка истории поиска
    private val historyListAdapter = SearchListAdapter(true, trackClickListener)

    // Инициализируем ручку для доступа к главному потоку
    private var mainThreadHandler = Handler(Looper.getMainLooper())

    // Переменная для TextWatcher
    private lateinit var simpleTextWatcher: TextWatcher

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализируем ViewBinding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем ViewModel и подписываемся на изменения состояний
        viewModel = ViewModelProvider(this, SearchViewModel.getViewModelFactory())[SearchViewModel::class.java]
        viewModel.observeState().observe(this) {
            render(it)
        }

        // Активируем toolbar для реализации возврата в главную активность по системной кнопке
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
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

        // Готовим RecyclerView
        binding.searchRecyclerView.adapter = searchListAdapter

        // Готовим RecyclerView для истории поиска
        binding.historyRecyclerView.adapter = historyListAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleTextWatcher.let { binding.inputEditText.removeTextChangedListener(it) }
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
        binding.searchRecyclerView.isVisible = true
        binding.searchHistoryView.isVisible = false
        binding.placeholderView.isVisible = false
        binding.progressBar.isVisible = false

        searchListAdapter.foundTracks.clear()
        searchListAdapter.foundTracks.addAll(trackList)
        searchListAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showTracksHistory(trackList: List<Track>) {
        binding.searchRecyclerView.isVisible = false
        binding.searchHistoryView.isVisible = true
        binding.placeholderView.isVisible = false
        binding.progressBar.isVisible = false

        historyListAdapter.foundTracks.clear()
        historyListAdapter.foundTracks.addAll(trackList)
        historyListAdapter.notifyDataSetChanged()
    }

    private fun showError(message: String) {
        binding.searchRecyclerView.isVisible = false
        binding.searchHistoryView.isVisible = false
        binding.placeholderView.isVisible = true
        binding.progressBar.isVisible = false

        binding.placeholderImage.setImageResource(R.drawable.no_connection)
        binding.placeholderButton.isVisible = true
        binding.placeholderMessage.text = message
    }

    private fun showEmpty(errorMessage: String) {
        binding.searchRecyclerView.isVisible = false
        binding.searchHistoryView.isVisible = false
        binding.placeholderView.isVisible = true
        binding.progressBar.isVisible = false

        binding.placeholderImage.setImageResource(R.drawable.nothing_found)
        binding.placeholderButton.isVisible = false
        binding.placeholderMessage.text = errorMessage
    }

}