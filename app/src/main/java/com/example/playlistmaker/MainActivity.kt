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
        val searchButton = findViewById<Button>(R.id.search)

//        // Обработка нажатия кнопки Поиск через способ 1 - реализацию абстрактного класса
//        val searchButtonClickListener: View.OnClickListener = object : View.OnClickListener {
//            override fun onClick(v: View?) {
//                Toast.makeText(this@MainActivity, "Запустили поиск!", Toast.LENGTH_SHORT).show()
//            }
//        }
//        searchButton.setOnClickListener(searchButtonClickListener)

        // Выполняем Intent в явном виде для активации страницы активити для Поиска
        searchButton.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        // Обработка нажатия на кнопку Медиатека
        val mediaButton = findViewById<Button>(R.id.media)

//        // Обработка нажатия кнопки Медиатека через способ 2 - лямбда
//        mediaButton.setOnClickListener {
//            Toast.makeText(this@MainActivity, "Запустили медиатеку!", Toast.LENGTH_SHORT).show()
//        }

        // Выполняем Intent в явном виде для активации страницы активити для Медиатеки
        mediaButton.setOnClickListener {
            val mediaIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)
        }

        // Обработка нажатия на кнопку Медиатека
        val settingsButton = findViewById<Button>(R.id.settings)

//        // Обработка нажатия кнопки Настройки через способ 2 - лямбда
//        settingsButton.setOnClickListener {
//            Toast.makeText(this@MainActivity, "Запустили настройки!", Toast.LENGTH_SHORT).show()
//        }

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