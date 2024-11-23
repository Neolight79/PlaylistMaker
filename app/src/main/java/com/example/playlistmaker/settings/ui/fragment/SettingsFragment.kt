package com.example.playlistmaker.settings.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    // Переменная для ViewModel
    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем ViewModel и подписываемся на изменения состояний
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        // Подписываемся на прилетающие интенты для вызова внешних операций
        viewModel.observeStateIntent().observe(viewLifecycleOwner) {
            startActivity(it)
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

    }

    // Отрисовываем установку флажка в соответствии с текущей темой
    private fun render(state: ThemeSettings) {
        binding.themeSwitcher.setChecked(state.isNightMode)
    }

}