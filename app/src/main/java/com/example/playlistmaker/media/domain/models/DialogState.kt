package com.example.playlistmaker.media.domain.models

sealed interface DialogState {
    object None : DialogState
    data class Dialog(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit,
        val onCancel: () -> Unit) : DialogState
}