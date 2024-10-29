package com.example.playlistmaker.settings.ui.view_model

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.sharing.domain.SharingInteractor

class SettingsViewModel(private val sharingInteractor: SharingInteractor,
                        private val settingsInteractor: SettingsInteractor,
                        application: Application): AndroidViewModel(application) {

    // LiveData для состояния переключателя темы
    private val stateLiveData = MutableLiveData<ThemeSettings>()
    fun observeState(): LiveData<ThemeSettings> {
        return stateLiveData
    }
    // LiveData для вызова внешних интентов
    private val stateIntent = SingleLiveEvent<Intent>()
    fun getStateIntent(): LiveData<Intent> {
        return stateIntent
    }

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
