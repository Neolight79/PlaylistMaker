package com.example.playlistmaker.settings.ui.view_model

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.sharing.domain.SharingInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val sharingInteractor: SharingInteractor,
                        private val settingsInteractor: SettingsInteractor): ViewModel() {

    // StateFlow для состояния переключателя темы (для режима Compose)
    private val _themeSettings = MutableStateFlow<ThemeSettings>(ThemeSettings(isNightMode = false))
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    // StateFlow для вызова внешних интентов (для режима Compose)
    private val _stateIntentCompose = MutableStateFlow<Intent>(Intent())
    val stateIntentCompose = _stateIntentCompose.asStateFlow()

    init {
        // Получаем из интерактора текущую тему
        _themeSettings.value = settingsInteractor.getThemeSettings()
    }

    // Передаём на запись новое значение темы
    fun switchTheme(isNightMode: Boolean) {
        val currentMode = ThemeSettings(isNightMode = isNightMode)
        settingsInteractor.updateThemeSetting(currentMode)
        _themeSettings.value = currentMode
    }

    // Функции передачи интента в LiveData для выполнения внешних операций
    fun shareApp() {
        _stateIntentCompose.value = sharingInteractor.shareApp().intent
    }

    fun mailToSupport() {
        _stateIntentCompose.value = sharingInteractor.openSupport().intent
    }

    fun userAgreement() {
        _stateIntentCompose.value = sharingInteractor.openTerms().intent
    }

}
