package com.example.playlistmaker.media.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.playlistmaker.R
import com.example.playlistmaker.main.ui.compose.ROUTE_MANAGE_PLAYLIST
import com.example.playlistmaker.main.ui.compose.ROUTE_PLAYLIST
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.domain.models.PlaylistsState
import com.example.playlistmaker.media.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.util.debounceCompose
import com.example.playlistmaker.util.ui.compose.Placeholder
import com.example.playlistmaker.util.ui.compose.PlaceholderButton
import com.example.playlistmaker.util.ui.compose.PlaceholderProgressBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistsScreen(
    navController: NavController,
    viewModel: PlaylistsViewModel = koinViewModel<PlaylistsViewModel>()
) {

    // Отслеживаем основной объект со статусом экрана плейлистов
    val playlistsState = viewModel.playlistsState.collectAsState().value

    LifecycleResumeEffect(Unit) {
        viewModel.fillData()
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlaceholderButton(stringResource(R.string.new_playlist)) {
            navController.navigate("$ROUTE_MANAGE_PLAYLIST/0")
        }
        when (playlistsState) {
            PlaylistsState.Loading -> PlaceholderProgressBar()
            PlaylistsState.Empty -> Placeholder(stringResource(R.string.no_playlist_message))
            is PlaylistsState.Playlists -> PlaylistsList(playlistsState.playlists) { playlist ->
                navController.navigate("$ROUTE_PLAYLIST/${playlist.playlistId}")
            }
        }
    }
}

@Composable
fun PlaylistsList(playlistsList: List<Playlist>, onClickAction: (playlist: Playlist) -> Unit) {
    LazyVerticalGrid(
        modifier = Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Fixed(2)) {
        items(playlistsList) {
            PlaylistItem(it) {
                onClickAction(it)
            }
        }
    }
}

@Composable
fun PlaylistItem(playlist: Playlist, onClickAction: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = debounceCompose(action = onClickAction))) {
        Spacer(modifier = Modifier.height(16.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlist.playlistImagePath)
                .crossfade(true)
                .build(),
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1F)
                    .clip(RoundedCornerShape(12.dp)),
            )
        Spacer(Modifier.height(4.dp))
        // Название плейлиста
        Text(text = playlist.playlistName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = colorResource(R.color.main_foreground),
                fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_regular))
            )
        )
        // Количество треков
        val tracksQuantityString = pluralStringResource(
            R.plurals.numberOfTracks,
            playlist.playlistTracksQuantity,
            playlist.playlistTracksQuantity)
        Text(text = tracksQuantityString,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = colorResource(R.color.main_foreground),
                fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_regular))
            )
        )
    }
}