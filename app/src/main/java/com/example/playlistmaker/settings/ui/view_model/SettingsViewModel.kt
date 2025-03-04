package com.example.playlistmaker.settings.ui.view_model

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.sharing.domain.SharingInteractor

class SettingsViewModel(private val sharingInteractor: SharingInteractor,
                        private val settingsInteractor: SettingsInteractor): ViewModel() {

    // LiveData для состояния переключателя темы
    private val stateLiveData = MutableLiveData<ThemeSettings>()
    fun observeState(): LiveData<ThemeSettings> = stateLiveData

    // LiveData для вызова внешних интентов
    private val stateIntent = SingleLiveEvent<Intent>()
    fun observeStateIntent(): LiveData<Intent> = stateIntent

    init {
        // Получаем из интерактора текущую тему
        stateLiveData.postValue(settingsInteractor.getThemeSettings())
    }

    // Передаём на запись новое значение темы
    fun switchTheme(isNightMode: Boolean) {
        val currentMode = ThemeSettings(isNightMode = isNightMode)
        settingsInteractor.updateThemeSetting(currentMode)
        stateLiveData.postValue(currentMode)
    }

    // Функции передачи интента в LiveData для выполнения внешних операций
    fun shareApp() {
        stateIntent.value = sharingInteractor.shareApp().intent
    }

    fun mailToSupport() {
        stateIntent.value = sharingInteractor.openSupport().intent
    }

    fun userAgreement() {
        stateIntent.value = sharingInteractor.openTerms().intent
    }

}
