package com.example.playlistmaker.sharing.domain.model

@Suppress("ArrayInDataClass")
data class EmailData(
    val emails: Array<String>,
    val subject: String,
    val text: String)
