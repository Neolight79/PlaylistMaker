package com.example.playlistmaker.settings.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    // Переменная для ViewBinding
    private lateinit var binding: ActivitySettingsBinding

    // Переменная для ViewModel
    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализируем ViewBinding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем ViewModel и подписываемся на изменения состояний
        viewModel.observeState().observe(this) {
            render(it)
        }
        // Подписываемся на прилетающие интенты для вызова внешних операций
        viewModel.getStateIntent().observe(this) {
            startActivity(it)
        }

        // Активируем тулбар для реализации возврата в главную активность по системной кнопке
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        // Инициализируем обработчика переключателя темы
        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        // Обработка нажатия на кнопку ПоделитьсяПриложением
        binding.settingsShareApp.setOnClickListener {
            viewModel.shareApp()
        }

        // Обработка нажатия на кнопку НаписатьВПоддержку
        binding.settingsMailToSupport.setOnClickListener {
            viewModel.mailToSupport()
        }

        // Обработка нажатия на кнопку Пользовательское соглашение
        binding.settingsUserAgreement.setOnClickListener {
            viewModel.userAgreement()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Отрисовываем установку флажка в соответствии с текущей темой
    private fun render(state: ThemeSettings) {
        binding.themeSwitcher.setChecked(state.isNightMode)
    }

}