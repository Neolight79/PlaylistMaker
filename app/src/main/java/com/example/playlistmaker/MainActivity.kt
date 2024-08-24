package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Обработка нажатия на кнопку Поиск
        val searchButton = findViewById<Button>(R.id.buttonSearch)

        // Выполняем Intent в явном виде для активации страницы активити для Поиска
        searchButton.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        // Обработка нажатия на кнопку Медиатека
        val mediaButton = findViewById<Button>(R.id.buttonMedia)

        // Выполняем Intent в явном виде для активации страницы активити для Медиатеки
        mediaButton.setOnClickListener {
            val mediaIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)
        }

        // Обработка нажатия на кнопку Настройки
        val settingsButton = findViewById<Button>(R.id.buttonSettings)

        // Выполняем Intent в явном виде для активации страницы активити Настроек
        settingsButton.setOnClickListener {
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