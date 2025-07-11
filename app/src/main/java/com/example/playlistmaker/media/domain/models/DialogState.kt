package com.example.playlistmaker.media.domain.models

sealed class DialogState {
    object None : DialogState()
    data class Dialog(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit,
        val onCancel: () -> Unit) : DialogState()
}