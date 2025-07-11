package com.example.playlistmaker.main.ui.compose

import androidx.benchmark.perfetto.ExperimentalPerfettoCaptureApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.playlistmaker.main.domain.model.BottomBarItem
import com.example.playlistmaker.media.ui.compose.MediaScreen
import com.example.playlistmaker.search.ui.compose.SearchScreen
import com.example.playlistmaker.settings.ui.compose.SettingsScreen
import com.example.playlistmaker.R
import com.example.playlistmaker.main.domain.model.BottomNavRoutes
import com.example.playlistmaker.media.ui.compose.ManagePlaylistScreen
import com.example.playlistmaker.media.ui.compose.PlaylistScreen
import com.example.playlistmaker.media.ui.view_model.ManagePlaylistViewModel
import com.example.playlistmaker.media.ui.view_model.PlaylistViewModel
import com.example.playlistmaker.player.ui.compose.PlayerScreen
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalPerfettoCaptureApi::class)
@Composable
fun MainScreen(bottomBarRoutes: List<BottomBarItem>) {

    val navController = rememberNavController()

    val bottomNavRoutes = BottomNavRoutes.entries.map { it.name }
    val showBottomBar = remember { mutableStateOf(true) }
    val isBottomBarDestination =
        navController.currentBackStackEntryAsState().value?.destination?.route in bottomNavRoutes

    // Если BottomBar должен появиться, то добавляем задержку в 300мс, чтобы не было конфликта с
    // версткой исчезающих экранов
    LaunchedEffect(isBottomBarDestination) {
        if (isBottomBarDestination) delay(300)
        showBottomBar.value = isBottomBarDestination
    }

    Scaffold(
        containerColor = colorResource(R.color.main_background),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar.value,
                enter = slideInVertically(
                    initialOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it }, animationSpec = tween(durationMillis = 100))
            ) {
                Column {
                    HorizontalDivider(thickness = 1.dp)
                    NavigationBar(
                        modifier = Modifier.defaultMinSize(minHeight = 72.dp),
                        containerColor = colorResource(R.color.main_background)) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        bottomBarRoutes.forEach { bottomBarRoute ->
                            NavigationBarItem(
                                modifier = Modifier.defaultMinSize(minHeight = 72.dp),
                                icon = {
                                    Icon(
                                        modifier = Modifier.padding(0.dp),
                                        imageVector = bottomBarRoute.icon,
                                        contentDescription = bottomBarRoute.label)
                                       },
                                label = {
                                    Text(text = bottomBarRoute.label,
                                        modifier = Modifier.padding(0.dp),
                                        style = TextStyle(
                                            fontSize = dimensionResource(R.dimen.artist_name_font_size).value.sp,
                                            fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
                                            fontFamily = FontFamily(Font(R.font.ys_display_regular))))
                                        },
                                selected = currentDestination?.route == bottomBarRoute.route.name,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colorResource(R.color.light_blue),
                                    selectedTextColor = colorResource(R.color.light_blue),
                                    unselectedIconColor = colorResource(R.color.main_foreground),
                                    unselectedTextColor = colorResource(R.color.main_foreground),
                                    indicatorColor = Color.Transparent
                                ),
                                onClick = { navController.navigate(bottomBarRoute.route.name)
                                {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoutes.Media.name,
            modifier = Modifier.padding(innerPadding)
        ) {
// Экран ПОИСК
            composable(
                route = BottomNavRoutes.Search.name,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                }) { SearchScreen(navController = navController) }
// Экран МЕДИАТЕКА
            composable(route = BottomNavRoutes.Media.name,
                enterTransition = {
                    when (initialState.destination.route) {
                        BottomNavRoutes.Search.name -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        }
                        BottomNavRoutes.Settings.name -> {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        }
                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        BottomNavRoutes.Search.name -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                        }
                        BottomNavRoutes.Settings.name -> {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                        }
                        else -> null
                    }
                }) { MediaScreen(navController = navController) }
// Экран НАСТРОЙКИ
            composable(route = BottomNavRoutes.Settings.name,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }) { SettingsScreen() }
// Экран ПЛЕЕР
            composable(
                route = "player/{trackID}",
                arguments = listOf(navArgument("trackID") { type = NavType.IntType }),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                }) { backStackEntry ->
                val trackID = backStackEntry.arguments?.getInt("trackID") ?: 0
                PlayerScreen(
                    navController = navController,
                    viewModel = koinViewModel<PlayerViewModel>(parameters = { parametersOf(trackID) }))
            }
// Экран СОЗДАНИЯ/РЕДАКТИРОВАНИЯ ПЛЕЙЛИСТА
            composable(
                route = "managePlaylist/{playlistID}",
                arguments = listOf(navArgument("playlistID") { type = NavType.IntType }),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                }) { backStackEntry ->
                val playlistID = backStackEntry.arguments?.getInt("playlistID") ?: 0
                ManagePlaylistScreen(
                    navController = navController,
                    viewModel = koinViewModel<ManagePlaylistViewModel>(parameters = { parametersOf(playlistID) }))
            }
// Экран ПЛЕЙЛИСТА
            composable(
                route = "playlist/{playlistID}",
                arguments = listOf(navArgument("playlistID") { type = NavType.IntType }),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                }) { backStackEntry ->
                val playlistID = backStackEntry.arguments?.getInt("playlistID") ?: 0
                PlaylistScreen(
                    navController = navController,
                    viewModel = koinViewModel<PlaylistViewModel>(parameters = { parametersOf(playlistID) }))
            }
        }
    }
}
