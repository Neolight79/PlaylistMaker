package com.example.playlistmaker.media.domain.models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue

data class BottomSheetState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val newState: SheetValue,
    val message: String?
)
