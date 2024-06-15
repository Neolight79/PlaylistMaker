package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Обработка нажатия кнопки Поиск через способ 1 - реализацию абстрактного класса
        val searchButton = findViewById<Button>(R.id.search)
        val searchButtonClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Запустили поиск!", Toast.LENGTH_SHORT).show()
            }
        }
        searchButton.setOnClickListener(searchButtonClickListener)

        // Обработка нажатия кнопки Медиатека через способ 2 - лямбда
        val mediaButton = findViewById<Button>(R.id.media)
        mediaButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Запустили медиатеку!", Toast.LENGTH_SHORT).show()
        }

        // Обработка нажатия кнопки Настройки через способ 2 - лямбда
        val settingsButton = findViewById<Button>(R.id.settings)
        settingsButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Запустили настройки!", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}