package com.example.playlistmaker.sharing.data

import com.example.playlistmaker.sharing.domain.model.EmailData
import com.example.playlistmaker.sharing.domain.model.IntentData

interface ExternalNavigator {
    fun shareLink(link: String): IntentData
    fun openLink(link: String): IntentData
    fun openEmail(emailData: EmailData): IntentData
}