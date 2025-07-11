package com.example.playlistmaker.sharing.data.impl

import android.content.Intent
import com.example.playlistmaker.sharing.data.ExternalNavigator
import com.example.playlistmaker.sharing.domain.model.EmailData
import com.example.playlistmaker.sharing.domain.model.IntentData
import androidx.core.net.toUri

class ExternalNavigatorImpl: ExternalNavigator {

    override fun shareText(text: String): IntentData {
        return IntentData(intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        })
    }

    override fun openLink(link: String): IntentData {
        return IntentData(intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = link.toUri()
        })
    }

    override fun openEmail(emailData: EmailData): IntentData {
        return IntentData(intent = Intent().apply {
            action = Intent.ACTION_SENDTO
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, emailData.emails)
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.text)
        })
    }

}