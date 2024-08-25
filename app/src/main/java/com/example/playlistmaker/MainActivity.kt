package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Обработка нажатия на кнопку Поиск
        // Выполняем Intent в явном виде для активации страницы активити для Поиска
        binding.buttonSearch.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        // Обработка нажатия на кнопку Медиатека
        // Выполняем Intent в явном виде для активации страницы активити для Медиатеки
        binding.buttonMedia.setOnClickListener {
            val mediaIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)
        }

        // Обработка нажатия на кнопку Настройки
        // Выполняем Intent в явном виде для активации страницы активити Настроек
        binding.buttonSettings.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}