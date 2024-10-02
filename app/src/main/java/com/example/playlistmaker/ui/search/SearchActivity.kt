package com.example.playlistmaker.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.App
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.impl.SearchHistoryImpl
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.SearchHistory
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.player.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private val searchInteractor: TracksInteractor by lazy {
        Creator.provideTracksInteractor()
    }

    private lateinit var binding: ActivitySearchBinding

    // Защита от DEBOUNCE для нажатий на элементы списков
    private var isClickAllowed = true

    // Инициализируем переменную для хранения запроса поиска
    private var searchString: String = SEARCH_DEF

    // Инициализируем переменные для RecyclerView списка найденных треков
    private val searchListAdapter = SearchListAdapter(false)
    private val trackList = mutableListOf<Track>()

    // Инициализируем переменные для RecyclerView списка истории поиска
    private val historyListAdapter = SearchListAdapter(true)
    private val searchHistoryTrackList = mutableListOf<Track>()

    // Инициализируем элементы для экрана вывода истории поиска
    private lateinit var searchHistory: SearchHistory

    // Инициализируем интерфейс для запуска поиска в новом потоке
    private val searchRunnable = Runnable { search() }

    // Инициализируем ручку для доступа к главному потоку
    private var mainThreadHandler: Handler? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем toolbar для реализации возврата в главную активность по системной кнопке
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        // Создаём handler для доступа из дополнительных потоков к главному
        mainThreadHandler = Handler(Looper.getMainLooper())

        // Загружаем историю поиска
        searchHistory = Creator.provideSearchHistory(searchHistoryTrackList)

        // Восстанавливаем содержимое строки поиска
        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH_DEF)
            binding.inputEditText.setText(searchString)
        }

        // Устанавливаем обработчик на кнопку очистки строки поиска
        binding.clearIcon.setOnClickListener {
            mainThreadHandler?.removeCallbacks(searchRunnable)
            binding.inputEditText.text.clear()
            hideMessage()
            trackList.clear()
            searchListAdapter.notifyDataSetChanged()
        }

        // Устанавливаем обработчик на кнопку обновления после ошибки
        binding.placeholderButton.setOnClickListener {
            mainThreadHandler?.removeCallbacks(searchRunnable)
            search()
        }

        // Реализуем механизм контроля ввода данных в строку поиска
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Код обработчика перед изменением текста
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    mainThreadHandler?.removeCallbacks(searchRunnable)
                    binding.clearIcon.visibility = View.GONE
                    if (searchHistory.isNotEmpty()) binding.searchHistoryView.visibility = View.VISIBLE
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
                } else {
                    binding.clearIcon.visibility = View.VISIBLE
                    searchString = s.toString()
                    binding.searchHistoryView.visibility = View.GONE
                    searchDebounce()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Код обработчика после изменения текста
            }
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)

        // Помимо поиска по DEBOUNCE можно запустить не дожидаясь двух секунд принудительно
        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mainThreadHandler?.removeCallbacks(searchRunnable)
                search()
            }
            false
        }

        // Добавляем обработчик для смены фокуса на поле ввода
        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            binding.searchHistoryView.visibility = if (hasFocus && binding.inputEditText.text.isEmpty() && searchHistory.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Готовим RecyclerView
        searchListAdapter.foundTracks = trackList
        binding.searchRecyclerView.adapter = searchListAdapter

        // Подключаем обработчик нажатия на элемент списка RecyclerView для списка найденных треков
        searchListAdapter.onItemClick = { track ->
            if (clickDebounce()) startPlayer(track)
        }

        // Подключаем обработчик нажатия на элемент списка RecyclerView для списка истории
        historyListAdapter.onItemClick = { track ->
            if (clickDebounce()) startPlayer(track)
        }

        // Подключаем обработчик нажатия на кнопку очистки истории
        historyListAdapter.onClearHistoryClick = {
            searchHistory.clearHistory()
            historyListAdapter.notifyDataSetChanged()
            binding.searchHistoryView.visibility = View.GONE
        }

        // Готовим RecyclerView для истории поиска
        historyListAdapter.foundTracks = searchHistoryTrackList
        binding.historyRecyclerView.adapter = historyListAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Переопределяем обработчик восстановления состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_STRING, searchString)
    }

    // Обработчик запуска поиска треков через интерактор
    private fun search() {

        // Показываем ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        // Запускаем поиск треков
        searchInteractor.searchTracks(binding.inputEditText.text.toString(), object : TracksInteractor.TracksConsumer {
            @SuppressLint("NotifyDataSetChanged")
            override fun consume(foundTracks: List<Track>) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    trackList.clear()
                    if (foundTracks.isNotEmpty()) {
                        trackList.addAll(foundTracks)
                        searchListAdapter.notifyDataSetChanged()
                        hideMessage()
                    } else {
                        showMessageNothingFound()
                    }
                }
            }
        })
    }

    private fun hideMessage() {
        if (binding.placeholderView.visibility != View.GONE)
            binding.placeholderView.visibility = View.GONE
    }

    private fun showMessageNothingFound() {
        binding.placeholderButton.visibility = View.GONE
        binding.placeholderImage.setImageResource(R.drawable.nothing_found)
        binding.placeholderView.visibility = View.VISIBLE
        binding.placeholderMessage.text = getString(R.string.nothing_found)
    }
// Обработка ошибок отложена до 16го спринта, поэтому пока деактивируем данный функционал
//    private fun showErrorMessage(errorText: String, errorDetails: String) {
//        binding.placeholderButton.visibility = View.VISIBLE
//        binding.placeholderImage.setImageResource(R.drawable.no_connection)
//        binding.placeholderMessage.text = errorText
//        binding.placeholderView.visibility = View.VISIBLE
//
//        if (errorText.isNotEmpty())
//            Toast.makeText(applicationContext, errorDetails, Toast.LENGTH_LONG).show()
//
//    }

    private fun searchDebounce() {
        mainThreadHandler?.removeCallbacks(searchRunnable)
        mainThreadHandler?.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            mainThreadHandler?.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun startPlayer(track: Track) {
        searchHistory.addTrack(track)
        historyListAdapter.notifyDataSetChanged()
        val playerIntent = Intent(this, PlayerActivity::class.java)
        playerIntent.putExtra("TRACK", track)
        startActivity(playerIntent)
    }

    companion object {
        const val SEARCH_STRING = "SEARCH_STRING"
        const val SEARCH_DEF = ""
        const val SEARCH_DEBOUNCE_DELAY = 2000L
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}