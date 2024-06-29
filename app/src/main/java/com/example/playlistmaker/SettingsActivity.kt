package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        // Обработка нажатия на кнопку ПоделитьсяПриложением
        val settingsShareApp = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.settingsShareApp)
        settingsShareApp.setOnClickListener {
            val shareAppIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_url))
                type = "text/plain"
            }
            startActivity(shareAppIntent)
        }

        // Обработка нажатия на кнопку НаписатьВПоддержку
        val settingsMailToSupport = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.settingsMailToSupport)
        settingsMailToSupport.setOnClickListener {
            val mailToSupportIntent = Intent().apply {
                action = Intent.ACTION_SENDTO
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subj))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
            }
            startActivity(mailToSupportIntent)
        }

        // Обработка нажатия на кнопку Пользовательское соглашение
        val settingsUserAgreement = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.settingsUserAgreement)
        settingsUserAgreement.setOnClickListener {
            val userAgreement = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(getString(R.string.user_agreement_url))
            }
            startActivity(userAgreement)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}