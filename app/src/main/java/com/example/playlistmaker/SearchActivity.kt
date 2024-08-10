package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    // Инициализируем переменную для хранения запроса поиска
    private var searchString: String = SEARCH_DEF

    // Инициализируем переменные для API
    private val itunesBaseUrl = "https://itunes.apple.com"
    private val entity = "song"
    private val country = "RU"
    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesService = retrofit.create(ItunesApi::class.java)

    // Инициализируем переменные для RecyclerView
    private val searchListAdapter = SearchListAdapter()
    private val trackList = ArrayList<Track>()
    //private val trackList = getMockTracks()   // Заполнение списка моковыми данными

    // Инициализируем объекты для элементов активити
    private lateinit var inputEditText: EditText
    private lateinit var rvSearchList: RecyclerView
    private lateinit var clearButton: ImageView
    // Инициализируем элементы для экрана вывода сообщений с ошибками
    private lateinit var placeholderView: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var placeholderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        // Получаем объекты элементов активити
        inputEditText = findViewById<EditText>(R.id.inputEditText)
        clearButton = findViewById<ImageView>(R.id.clearIcon)
        placeholderView = findViewById<View>(R.id.placeholderView)
        placeholderImage = findViewById<ImageView>(R.id.placeholderImage)
        placeholderMessage = findViewById<TextView>(R.id.placeholderMessage)
        placeholderButton = findViewById<Button>(R.id.placeholderButton)

        //Восстанавливаем содержимое строки поиска
        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH_DEF)
            inputEditText.setText(searchString)
        }

        // Устанавливаем обработчик на кнопку очистки строки поиска
        clearButton.setOnClickListener {
            inputEditText.setText("")
            trackList.clear()
            searchListAdapter.notifyDataSetChanged()
        }

        // Устанавливаем обработчик на кнопку обновления после ошибки
        placeholderButton.setOnClickListener {
            search()
        }

        // Реализуем механизм контроля ввода данных в строку поиска
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Код обработчика перед изменением текста
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton.visibility = View.GONE
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
                } else {
                    clearButton.visibility = View.VISIBLE
                    searchString = s.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Код обработчика после изменения текста
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        // Временно добавляем для запуска поиска по нажатию Done
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search()
                true
            }
            false
        }

        // Готовим RecyclerView
        searchListAdapter.foundTracks = trackList
        rvSearchList = findViewById<RecyclerView>(R.id.searchRecyclerView)
        rvSearchList.adapter = searchListAdapter

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
        itunesService.getTracks(entity, country, inputEditText.text.toString())
            .enqueue(object : Callback<ItunesResponse> {
                override fun onResponse(call: Call<ItunesResponse>,
                                        response: Response<ItunesResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackList.clear()
                                trackList.addAll(response.body()?.results!!)
                                searchListAdapter.notifyDataSetChanged()
                                hideMessage()
                            } else {
                                showMessageNothingFound()
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
        if (placeholderView.visibility != View.GONE)
            placeholderView.visibility = View.GONE
    }

    private fun showMessageNothingFound() {
        placeholderButton.visibility = View.GONE
        placeholderImage.setImageResource(R.drawable.nothing_found)
        placeholderMessage.text = getString(R.string.nothing_found)
        placeholderView.visibility = View.VISIBLE
    }

    private fun showErrorMessage(errorText: String, errorDetails: String) {
        placeholderButton.visibility = View.VISIBLE
        placeholderImage.setImageResource(R.drawable.no_connection)
        placeholderMessage.text = errorText
        placeholderView.visibility = View.VISIBLE

        if (errorText.isNotEmpty())
            Toast.makeText(applicationContext, errorDetails, Toast.LENGTH_LONG).show()

    }

    companion object {
        const val SEARCH_STRING = "SEARCH_STRING"
        const val SEARCH_DEF = ""
    }
}