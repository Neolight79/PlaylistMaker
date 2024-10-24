package com.example.playlistmaker.settings.ui.view_model

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.settings.domain.model.ThemeSettings

class SettingsViewModel(application: Application): AndroidViewModel(application) {

    // Описание сущностей уровня класса
    companion object {

        fun getViewModelFactory(): ViewModelProvider.Factory {
            return viewModelFactory {
                    initializer {
                        SettingsViewModel(this[APPLICATION_KEY] as Application)
                }
            }
        }

    }

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

    private val sharingInteractor = Creator.provideSharingInteractor(getApplication())
    private val settingsInteractor = Creator.provideSettingsInteractor(getApplication())

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
