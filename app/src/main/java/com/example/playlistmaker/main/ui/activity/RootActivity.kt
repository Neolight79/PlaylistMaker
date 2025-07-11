package com.example.playlistmaker.main.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Search
import com.example.playlistmaker.R
import com.example.playlistmaker.main.domain.model.BottomBarItem
import com.example.playlistmaker.main.domain.model.BottomNavRoutes
import com.example.playlistmaker.main.ui.compose.MainScreen

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bottomBarRoutes = listOf(
            BottomBarItem(
                label = getString(R.string.search),
                icon =  Icons.Outlined.Search,
                route = BottomNavRoutes.Search
            ),
            BottomBarItem(
                label = getString(R.string.mediateka),
                icon = Icons.Filled.LibraryMusic,
                route = BottomNavRoutes.Media
            ),
            BottomBarItem(
                label = getString(R.string.settings),
                icon = Icons.Filled.Settings,
                route = BottomNavRoutes.Settings
            ))
        setContent {
            MainScreen(bottomBarRoutes)
        }
    }
}