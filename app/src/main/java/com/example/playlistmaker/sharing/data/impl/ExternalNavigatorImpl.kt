package com.example.playlistmaker.sharing.data.impl

import android.content.Intent
import android.net.Uri
import com.example.playlistmaker.sharing.data.ExternalNavigator
import com.example.playlistmaker.sharing.domain.model.EmailData
import com.example.playlistmaker.sharing.domain.model.IntentData

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
            data = Uri.parse(link)
        })
    }

    override fun openEmail(emailData: EmailData): IntentData {
        return IntentData(intent = Intent().apply {
            action = Intent.ACTION_SENDTO
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, emailData.emails)
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.text)
        })
    }

}