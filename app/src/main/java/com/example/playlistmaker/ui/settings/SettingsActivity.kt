package com.example.playlistmaker.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        // Инициализируем переключатель темы в соответствии с текущей и активируем обработчик
        binding.themeSwitcher.setChecked((applicationContext as App).darkTheme)

        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
        }

        // Обработка нажатия на кнопку ПоделитьсяПриложением
        binding.settingsShareApp.setOnClickListener {
            val shareAppIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_url))
                type = "text/plain"
            }
            startActivity(shareAppIntent)
        }

        // Обработка нажатия на кнопку НаписатьВПоддержку
        binding.settingsMailToSupport.setOnClickListener {
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
        binding.settingsUserAgreement.setOnClickListener {
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