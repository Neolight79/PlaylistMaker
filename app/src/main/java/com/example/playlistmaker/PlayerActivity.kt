@file:Suppress("DEPRECATION")

package com.example.playlistmaker

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        // Восстанавливаем данные трека из Json
        val track: Track = intent.getSerializableExtra(TRACK) as Track

        // Наполняем активити данными объекта
        Glide.with(this)
            .load(track.artworkUrl512)
            .transform(
                CenterCrop(), RoundedCorners(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                8F, this.resources.displayMetrics).toInt())
            )
            .placeholder(R.drawable.placeholder)
            .into(binding.artwork)

        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName

        // Присваиваем значения деталей трека
        binding.durationDetailsData.text = track.trackTimeString
        binding.albumDetailsData.text = checkNullForDetails(track.collectionName, binding.albumGroup)
        val releaseDate = checkNullForDetails(track.releaseDate, binding.yearGroup)
        binding.yearDetailsData.text = if (releaseDate.isEmpty()) "" else releaseDate.substring(0, 4)
        binding.genreDetailsData.text = checkNullForDetails(track.primaryGenreName, binding.genreGroup)
        binding.countryDetailsData.text = checkNullForDetails(track.country, binding.countryGroup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkNullForDetails(text: String?, groupView: View): String {
        groupView.isVisible = !text.isNullOrEmpty()
        return if (text.isNullOrEmpty()) "" else text
    }

    companion object {
        const val TRACK = "TRACK"
    }
}