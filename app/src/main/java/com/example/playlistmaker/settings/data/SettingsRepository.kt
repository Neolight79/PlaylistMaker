package com.example.playlistmaker.settings.data

import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.sharing.domain.model.EmailData

interface SettingsRepository {
    fun getThemeSettings(): ThemeSettings
    fun updateThemeSetting(settings: ThemeSettings)
    fun getShareAppLink(): String
    fun getSupportEmailData(): EmailData
    fun getTermsLink(): String
}