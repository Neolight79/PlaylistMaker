package com.example.playlistmaker.main.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    val label: String,
    val icon : ImageVector,
    val route: BottomNavRoutes
)