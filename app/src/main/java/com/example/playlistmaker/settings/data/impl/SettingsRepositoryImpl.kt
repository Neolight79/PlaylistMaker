package com.example.playlistmaker.settings.data.impl

import android.content.Context
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.data.SettingsRepository
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.sharing.domain.model.EmailData

class SettingsRepositoryImpl(private val context: Context): SettingsRepository {

    override fun getThemeSettings(): ThemeSettings {
        return ThemeSettings(isNightMode = (context.applicationContext as App).darkTheme)
    }

    override fun updateThemeSetting(settings: ThemeSettings) {
        (context.applicationContext as App).switchTheme(settings.isNightMode)
    }

    override fun getShareAppLink(): String {
        return context.getString(R.string.share_app_url)
    }

    override fun getSupportEmailData(): EmailData {
        return EmailData(
            emails = arrayOf(context.getString(R.string.support_email)),
            subject = context.getString(R.string.support_subj),
            text = context.getString(R.string.support_text)
        )
    }

    override fun getTermsLink(): String {
        return context.getString(R.string.user_agreement_url)
    }

}