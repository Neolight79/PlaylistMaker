package com.example.playlistmaker

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        // Создаём переменные для элементов активити
        val trackArtwork = findViewById<ImageView>(R.id.artwork)
        val trackName = findViewById<TextView>(R.id.trackName)
        val artistName = findViewById<TextView>(R.id.artistName)
        // Переменные для данных деталей трека
        val durationDetailsData = findViewById<TextView>(R.id.durationDetailsData)
        val albumDetailsData = findViewById<TextView>(R.id.albumDetailsData)
        val yearDetailsData = findViewById<TextView>(R.id.yearDetailsData)
        val genreDetailsData = findViewById<TextView>(R.id.genreDetailsData)
        val countryDetailsData = findViewById<TextView>(R.id.countryDetailsData)
        // Переменные для групп управления видимостью деталей о треке
        val albumGroup = findViewById<androidx.constraintlayout.widget.Group>(R.id.albumGroup)
        val yearGroup = findViewById<androidx.constraintlayout.widget.Group>(R.id.yearGroup)
        val genreGroup = findViewById<androidx.constraintlayout.widget.Group>(R.id.genreGroup)
        val countryGroup = findViewById<androidx.constraintlayout.widget.Group>(R.id.countryGroup)

        // Восстанавливаем данные трека из Json
        val json: String? = intent.getStringExtra("TRACK")
        class Token : TypeToken<Track>()
        val track: Track = Gson().fromJson(json, Token().type)

        // Наполняем активити данными объекта
        Glide.with(this)
            .load(track.artworkUrl100.replaceAfterLast('/',"512x512bb.jpg"))
            .transform(
                CenterCrop(), RoundedCorners(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                8F, this.resources.displayMetrics).toInt())
            )
            .placeholder(R.drawable.placeholder)
            .into(trackArtwork)

        trackName.text = track.trackName
        artistName.text = track.artistName

        // Присваиваем значения деталей трека
        val dateFormat by lazy { SimpleDateFormat("mm:ss", Locale.getDefault()) }
        durationDetailsData.text = dateFormat.format(track.trackTimeMillis)
        albumDetailsData.text = checkNullForDetails(track.collectionName, albumGroup)
        val releaseDate = checkNullForDetails(track.releaseDate, yearGroup)
        yearDetailsData.text = if (releaseDate.isEmpty()) "" else releaseDate.substring(0, 4)
        genreDetailsData.text = checkNullForDetails(track.primaryGenreName, genreGroup)
        countryDetailsData.text = checkNullForDetails(track.country, countryGroup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkNullForDetails(text: String?, groupView: View): String {
        if (text.isNullOrEmpty()) {
            groupView.visibility = View.GONE
            return ""
        } else {
            groupView.visibility = View.VISIBLE
            return text
        }
    }
}