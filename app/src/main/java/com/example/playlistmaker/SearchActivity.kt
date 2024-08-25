package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var binding: ActivitySearchBinding

    // Инициализируем переменную для хранения запроса поиска
    private var searchString: String = SEARCH_DEF

    // Инициализируем переменные для API
    private val itunesBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesService = retrofit.create(ItunesApi::class.java)

    // Инициализируем переменные для RecyclerView списка найденных треков
    private val searchListAdapter = SearchListAdapter(false)
    private val trackList = mutableListOf<Track>()

    // Инициализируем переменные для RecyclerView списка истории поиска
    private val historyListAdapter = SearchListAdapter(true)
    private val searchHistoryTrackList = mutableListOf<Track>()

    // Инициализируем элементы для экрана вывода истории поиска
    private lateinit var searchHistory: SearchHistory

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        // Загружаем историю поиска
        searchHistory = SearchHistory((applicationContext as App).sharedPrefs, searchHistoryTrackList)

        //Восстанавливаем содержимое строки поиска
        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH_DEF)
            binding.inputEditText.setText(searchString)
        }

        // Устанавливаем обработчик на кнопку очистки строки поиска
        binding.clearIcon.setOnClickListener {
            binding.inputEditText.text.clear()
            hideMessage()
            trackList.clear()
            searchListAdapter.notifyDataSetChanged()
        }

        // Устанавливаем обработчик на кнопку обновления после ошибки
        binding.placeholderButton.setOnClickListener {
            search()
        }

        // Реализуем механизм контроля ввода данных в строку поиска
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Код обработчика перед изменением текста
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.clearIcon.visibility = View.GONE
                    if (searchHistory.isNotEmpty()) binding.searchHistoryView.visibility = View.VISIBLE
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
                } else {
                    binding.clearIcon.visibility = View.VISIBLE
                    searchString = s.toString()
                    binding.searchHistoryView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Код обработчика после изменения текста
            }
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)

        // Временно добавляем для запуска поиска по нажатию Done
        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
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
        //rvSearchList = findViewById(R.id.searchRecyclerView)
        binding.searchRecyclerView.adapter = searchListAdapter

        // Подключаем обработчик нажатия на элемент списка RecyclerView для списка найденных треков
        searchListAdapter.onItemClick = { track ->
            searchHistory.addTrack(track)
            historyListAdapter.notifyDataSetChanged()
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra("TRACK", track)
            startActivity(playerIntent)
        }

        // Подключаем обработчик нажатия на элемент списка RecyclerView для списка истории
        historyListAdapter.onItemClick = { track ->
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra("TRACK", track)
            startActivity(playerIntent)
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

    // Обработчик запуска обращения по API для получения результатов поиска
    private fun search() {
        itunesService.getTracks(binding.inputEditText.text.toString())
            .enqueue(object : Callback<ItunesResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ItunesResponse>,
                                        response: Response<ItunesResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val foundTracks: MutableList<Track>? = response.body()?.results?.toMutableList()
                            if (foundTracks.isNullOrEmpty()) {
                                showMessageNothingFound()
                            } else {
                                trackList.clear()
                                trackList.addAll(foundTracks)
                                searchListAdapter.notifyDataSetChanged()
                                hideMessage()
                            }
                        }
                        else -> showErrorMessage(getString(R.string.errorCommon), response.code().toString())
                    }

                }

                override fun onFailure(call: Call<ItunesResponse>, t: Throwable) {
                    showErrorMessage(getString(R.string.no_connection), t.message.toString())
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

    private fun showErrorMessage(errorText: String, errorDetails: String) {
        binding.placeholderButton.visibility = View.VISIBLE
        binding.placeholderImage.setImageResource(R.drawable.no_connection)
        binding.placeholderMessage.text = errorText
        binding.placeholderView.visibility = View.VISIBLE

        if (errorText.isNotEmpty())
            Toast.makeText(applicationContext, errorDetails, Toast.LENGTH_LONG).show()

    }

    companion object {
        const val SEARCH_STRING = "SEARCH_STRING"
        const val SEARCH_DEF = ""
    }
}