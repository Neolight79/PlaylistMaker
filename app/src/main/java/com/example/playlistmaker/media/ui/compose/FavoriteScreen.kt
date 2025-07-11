package com.example.playlistmaker.media.ui.compose

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.models.FavoriteState
import com.example.playlistmaker.media.ui.view_model.FavoriteViewModel
import com.example.playlistmaker.search.domain.models.Track
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.playlistmaker.util.debounceCompose
import com.example.playlistmaker.util.ui.compose.Placeholder
import com.example.playlistmaker.util.ui.compose.PlaceholderProgressBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoriteScreen(
    navController: NavController,
    viewModel: FavoriteViewModel = koinViewModel<FavoriteViewModel>()
) {

    // Отслеживаем основной объект со статусом экрана избранных треков
    val favoriteState = viewModel.favoriteState.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (favoriteState) {
            FavoriteState.Loading -> PlaceholderProgressBar()
            FavoriteState.Empty -> Placeholder(stringResource(R.string.media_empty_message))
            is FavoriteState.TracksFavorite -> FavoriteTracksList(favoriteState.trackList) { track ->
                navController.navigate("player/${track.trackId}")
            }
        }
    }
}

@Composable
fun FavoriteTracksList(trackList: List<Track>, onClickAction: (track: Track) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(trackList) {
            ListItem(it) {
                onClickAction(it)
            }
        }
    }
}

@Composable
fun ListItem(track: Track, onLongClickAction: () -> Unit = { }, onClickAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.settings_row_height))
            .combinedClickable(
                onClick = debounceCompose(action = onClickAction),
                onLongClick = onLongClickAction
            )
    ) {
        // Обложка альбома
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(66.dp)
                .padding(start = 12.dp, top = 8.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.artworkUrl100)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.list_image_size))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.image_corner_radius))),
            )
        }
        // Текстовые данные
        Column(modifier = Modifier
            .weight(1f)
            .padding(top = 14.dp)) {
            // Название трека
            Text(text = track.trackName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = colorResource(R.color.main_foreground),
                    fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular))
                )
            )
            // Исполнитель и продолжительность трека
            Row(modifier = Modifier.height(13.dp).width(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = track.artistName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = colorResource(R.color.light_gray_text),
                        fontSize = dimensionResource(R.dimen.search_item_small_text_size).value.sp,
                        fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                        fontFamily = FontFamily(Font(R.font.ys_display_regular))
                    ),
                    modifier = Modifier.weight(1F)
                )
                Icon(modifier = Modifier.width(13.dp),
                    painter = painterResource(id = R.drawable.round_glif),
                    tint = colorResource(R.color.light_gray_text),
                    contentDescription = null
                )
                Text(text = track.trackTimeString,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = colorResource(R.color.light_gray_text),
                        fontSize = dimensionResource(R.dimen.search_item_small_text_size).value.sp,
                        fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                        fontFamily = FontFamily(Font(R.font.ys_display_regular))
                    )
                )
            }
        }
        // Стрелка вправо
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(36.dp)
                .padding(end = 12.dp, top = 18.dp)) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                tint = colorResource(R.color.light_gray_text),
                contentDescription = null
            )
        }
    }
}
