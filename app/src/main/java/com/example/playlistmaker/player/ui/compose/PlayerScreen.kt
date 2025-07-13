package com.example.playlistmaker.player.ui.compose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.playlistmaker.R
import com.example.playlistmaker.main.ui.compose.ROUTE_MANAGE_PLAYLIST
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.ui.compose.ListItemPlaceholder
import com.example.playlistmaker.util.ui.compose.Placeholder
import com.example.playlistmaker.util.ui.compose.PlaceholderButton
import com.example.playlistmaker.util.ui.compose.PlaceholderProgressBar
import com.example.playlistmaker.player.domain.models.PlayerState
import com.example.playlistmaker.player.service.MusicService
import com.example.playlistmaker.player.service.NOTIFICATION_TEXT
import com.example.playlistmaker.player.service.NOTIFICATION_TITLE
import com.example.playlistmaker.player.service.SONG_URL
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.LostConnectionBroadcastReceiver
import com.example.playlistmaker.util.MusicServiceConnection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isShowBottomSheetMenu = remember { mutableStateOf(false) }
    val bottomSheetPlaylistsState = rememberModalBottomSheetState()
    val serviceConnection = remember { MusicServiceConnection(viewModel) }
    val lostConnectionBroadcastReceiver = remember { LostConnectionBroadcastReceiver() }
    val currentUrl = remember { mutableStateOf("") }
    val notificationText = remember { mutableStateOf("") }
    val isPlaying = remember { mutableStateOf(false) }
    val isServiceBound = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Если выдали разрешение — запускаем сервис.
            isServiceBound.value = bindMusicService(context, serviceConnection, currentUrl.value, notificationText.value)
        } else {
            // Иначе просто покажем ошибку
            Toast.makeText(context, context.getString(R.string.cant_start_foreground_service), Toast.LENGTH_LONG).show()
        }
    }

    // Отслеживаем основное состояние плеера
    val playerState = viewModel.playerState.collectAsState().value
    // Отслеживаем текущее состояние проигрывания
    val playStatusState = viewModel.playStatusState.collectAsState().value
    // Отслеживаем состояние BottomSheet
    val bottomSheetState = viewModel.bottomSheetState.collectAsState().value
    // Отслеживаем состояние для вывода списка плейлистов
    val playlistsState = viewModel.playlistsState.collectAsState().value

    LifecycleResumeEffect(Unit) {
        viewModel.refillPlaylists()
        ContextCompat.registerReceiver(context,
            lostConnectionBroadcastReceiver,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"),
            ContextCompat.RECEIVER_NOT_EXPORTED)
        viewModel.stopForegroundPlayerMode()
        onPauseOrDispose {
            context.unregisterReceiver(lostConnectionBroadcastReceiver)
            if (isPlaying.value) viewModel.startForegroundPlayerMode()
        }
    }

    LaunchedEffect(playStatusState) {
        isPlaying.value = playStatusState.isPlaying
    }

    LaunchedEffect(bottomSheetState) {
        if (!bottomSheetState.message.isNullOrEmpty()) {
            Toast.makeText(context, bottomSheetState.message, Toast.LENGTH_LONG).show()
            viewModel.onBottomSheetChangedState(bottomSheetState.newState)
        }
        when (bottomSheetState.newState) {
            SheetValue.Hidden -> {
                scope.launch { bottomSheetPlaylistsState.hide() }.invokeOnCompletion {
                    if (!bottomSheetPlaylistsState.isVisible) isShowBottomSheetMenu.value = false
                }
            }
            SheetValue.Expanded -> {
                isShowBottomSheetMenu.value = true
                bottomSheetPlaylistsState.expand()
            }
            SheetValue.PartiallyExpanded -> {
                isShowBottomSheetMenu.value = true
                bottomSheetPlaylistsState.partialExpand()
            }
        }
    }

    when (playerState) {
        is PlayerState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(200.dp))
                PlaceholderProgressBar()
            }
        }
        is PlayerState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Placeholder(
                    message = playerState.message,
                    topMarginRes = R.dimen.search_placeholder_top_margin,
                    imageRes = R.drawable.no_connection)
                PlaceholderButton(stringResource(R.string.refresh)) {
                    viewModel.loadTrackData()
                }
            }
        }
        is PlayerState.Empty -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Placeholder(
                    message = playerState.errorMessage,
                    topMarginRes = R.dimen.search_placeholder_top_margin,
                    imageRes = R.drawable.nothing_found
                )
            }
        }
        is PlayerState.Content -> {
            currentUrl.value = playerState.trackModel.previewUrl
            notificationText.value = "${playerState.trackModel.artistName} - ${playerState.trackModel.trackName}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // На версиях ниже Android 13 —
                // можно сразу стартовать сервис.
                LaunchedEffect(Unit) {
                    isServiceBound.value = bindMusicService(context, serviceConnection, currentUrl.value, notificationText.value)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    if (isServiceBound.value) unbindMusicService(context, serviceConnection)
                }
            }

            BottomSheetPlaylists(
                visible = isShowBottomSheetMenu.value,
                playlists = playlistsState,
                bottomSheetPlaylistsState = bottomSheetPlaylistsState,
                onDismissRequest = {
                    viewModel.onBottomSheetChangedState(SheetValue.Hidden)
                },
                onNewPlaylist = {
                    viewModel.onBottomSheetChangedState(SheetValue.Hidden)
                    navController.navigate("$ROUTE_MANAGE_PLAYLIST/0")
                },
                onClickPlaylist = { playlist ->
                    viewModel.onPlaylistClicked(playlist, bottomSheetPlaylistsState.currentValue)
                }
            )

            // Здесь выводим содержимое экрана плеера
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.main_background)),
                horizontalAlignment = Alignment.Start
            ) {
                // Сначала шапка со стрелкой Назад
                HeaderBlock(onClickAction = {
                    navController.navigateUp()
                })
                // Выводим информацию о треке с обложкой
                PlaylistInfo(playerState.trackModel)
                Spacer(modifier = Modifier.height(36.dp))
                // Контейнер для кнопок экрана
                ButtonsBlock(
                    isFavorite = playerState.trackModel.isFavorite,
                    isPlaying = playStatusState.isPlaying,
                    onAddToPlaylistClick = {
                        viewModel.onAddToPlaylistClicked()
                    },
                    onPlayButtonClick = {
                        if (!isServiceBound.value) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            // Проверяем на наличие привязанной службы, если служба не привязана, то
                            // просим дать разрешения
                            if (!isServiceBound.value) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.data= Uri.fromParts("package", context.packageName, null)
                                context.startActivity(intent)
                            }
                        }
                        viewModel.playbackControl()
                    },
                    onFavoriteClick = {
                        viewModel.onFavoriteClicked()
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Контейнер со счётчиком
                CounterBlock(playStatusState.currentPosition)
                Spacer(modifier = Modifier.height(16.dp))
                // Контейнер для деталей о треке
                DetailsBlock(playerState.trackModel)
            }
            }
        }
}

fun bindMusicService(context: Context, serviceConnection: ServiceConnection, currentUrl: String, notificationText: String): Boolean {
    return when (currentUrl.isNotEmpty()) {
        true -> {
            val intent = Intent(context, MusicService::class.java).apply {
                putExtra(SONG_URL, currentUrl)
                putExtra(NOTIFICATION_TITLE, context.getString(context.applicationInfo.labelRes))
                putExtra(NOTIFICATION_TEXT, notificationText)
            }
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        false -> false
    }
}

fun unbindMusicService(context: Context, serviceConnection: ServiceConnection) {
    context.unbindService(serviceConnection)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPlaylists(visible: Boolean,
                    playlists: List<Playlist>,
                    bottomSheetPlaylistsState: SheetState,
                    onDismissRequest: () -> Unit,
                    onNewPlaylist: () -> Unit,
                    onClickPlaylist: (Playlist) -> Unit
) {
    if (visible) {
        ModalBottomSheet(
            shape = RoundedCornerShape(16.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(50.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(44))
                        .background(colorResource(R.color.handle))
                ) },
            onDismissRequest = onDismissRequest,
            containerColor = colorResource(R.color.main_background),
            sheetState = bottomSheetPlaylistsState) {
            // Здесь выводим список плейлистов для выбора и возможность создать новый плейлист
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                // Заголовок списка
                ListItemPlaceholder(stringResource(R.string.desc_add_to_playlist), 52)
                // Кнопка создания плейлиста
                Box(modifier = Modifier.fillMaxWidth().height(52.dp),
                    contentAlignment = Alignment.Center) {
                    PlaceholderButton(
                        title = stringResource(R.string.new_playlist),
                        onClick = onNewPlaylist)
                }
                // Список плейлистов
                LazyColumn(Modifier.fillMaxSize()) {
                    items(playlists) {
                        PlaylistListItem(
                            playlist = it,
                            onClickAction = {
                                onClickPlaylist(it)
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun CounterBlock(currentPosition: String) {
    Box(modifier = Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.Center) {
        Text(text = currentPosition,
            maxLines = 1,
            style = TextStyle(
                color = colorResource(R.color.main_foreground),
                fontSize = dimensionResource(R.dimen.artist_name_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            ))
    }
}

@Composable
fun ButtonsBlock(
    isFavorite: Boolean,
    isPlaying: Boolean,
    onAddToPlaylistClick: () -> Unit,
    onPlayButtonClick: () -> Unit,
    onFavoriteClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(84.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        // Кнопка добавления трека в плейлист
        Button(
            shape = CircleShape,
            modifier = Modifier.size(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.main_foreground).copy(alpha = 0.25F)),
            contentPadding = PaddingValues(0.dp),
            onClick = onAddToPlaylistClick) {
            Icon(
                imageVector = Icons.Filled.AddToPhotos,
                contentDescription = "Localized description",
                tint = colorResource(R.color.white),
                modifier = Modifier.scale(1.2F)
            )
        }
        Spacer(modifier = Modifier.weight(1F))
        // Кнопка управления воспроизведением
        IconButton(
            modifier = Modifier.size(84.dp),
            onClick = onPlayButtonClick) {
            Icon(
                painter = if (isPlaying)
                    painterResource(R.drawable.pausebutton_glif)
                else
                    painterResource(R.drawable.playbutton_glif),
                contentDescription = null,
                tint = colorResource(R.color.main_foreground)
            )
        }
        Spacer(modifier = Modifier.weight(1F))
        // Кнопка добавления трека в избранное
        Button(
            shape = CircleShape,
            modifier = Modifier.size(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.main_foreground).copy(alpha = 0.25F)),
            contentPadding = PaddingValues(0.dp),
            onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite)
                    Icons.Filled.Favorite
                else
                    Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite)
                    colorResource(R.color.favorite_color)
                else
                    colorResource(R.color.white),
                modifier = Modifier.scale(1.3F)
            )
        }
    }
}

@Composable
fun DetailsBlock(track: Track) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().weight(1F), verticalAlignment = Alignment.CenterVertically) {
            // Длительность
            DetailsText(stringResource(R.string.duration), track.trackTimeString) {
                Spacer(modifier = Modifier.width(12.dp).weight(1F))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().weight(1F), verticalAlignment = Alignment.CenterVertically) {
            // Альбом
            DetailsText(stringResource(R.string.album), track.collectionName) {
                Spacer(modifier = Modifier.width(12.dp).weight(1F))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().weight(1F), verticalAlignment = Alignment.CenterVertically) {
            // Год
            DetailsText(stringResource(R.string.year), track.releaseDate) {
                Spacer(modifier = Modifier.width(12.dp).weight(1F))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().weight(1F), verticalAlignment = Alignment.CenterVertically) {
            // Жанр
            DetailsText(stringResource(R.string.genre), track.primaryGenreName) {
                Spacer(modifier = Modifier.width(12.dp).weight(1F))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().weight(1F), verticalAlignment = Alignment.CenterVertically) {
            // Страна
            DetailsText(stringResource(R.string.country), track.country) {
                Spacer(modifier = Modifier.width(12.dp).weight(1F))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DetailsText(textTitle: String, textDetail: String?, spacer: @Composable (() -> Unit)) {
    Text(text = textTitle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = colorResource(R.color.light_gray_text),
            fontSize = dimensionResource(R.dimen.details_text_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_regular))
        ))
    spacer()
    Text(text = textDetail ?: "",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = colorResource(R.color.main_foreground),
            fontSize = dimensionResource(R.dimen.details_text_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_regular))
        ))
}

@Composable
fun PlaylistInfo(track: Track) {
    // Контейнер для обложки
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1F).padding(24.dp)
    ) {
        // Изображение обложки
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.artworkUrl512)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1F)
                .clip(RoundedCornerShape(8.dp))
        )
    }
    // Контейнер для названия трека
    Text(text = track.trackName,
        modifier = Modifier.padding(horizontal = 24.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = colorResource(R.color.main_foreground),
            fontSize = dimensionResource(R.dimen.title_font_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_medium))
        ))
    Spacer(modifier = Modifier.height(12.dp))
    // Контейнер для имени исполнителя
    Text(text = track.artistName,
        modifier = Modifier.padding(horizontal = 24.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = colorResource(R.color.main_foreground),
            fontSize = dimensionResource(R.dimen.artist_name_font_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_medium))
        ))

}

@Composable
fun HeaderBlock(onClickAction: () -> Unit) {
    Box(
        modifier = Modifier.size(56.dp)
    ) {
        IconButton(
            modifier = Modifier.fillMaxSize(),
            onClick = onClickAction) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Localized description",
                tint = colorResource(R.color.main_foreground)
            )
        }
    }
}

@Composable
fun PlaylistListItem(playlist: Playlist, onClickAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.settings_row_height))
            .clickable(onClick = onClickAction)
    ) {
        // Обложка альбома
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(66.dp)
                .padding(start = 12.dp, top = 8.dp)) {
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
                    .size(dimensionResource(R.dimen.list_image_size))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.image_corner_radius))),
            )
        }
        // Текстовые данные
        Column(modifier = Modifier
            .weight(1f)
            .padding(top = 14.dp)) {
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
                    fontSize = dimensionResource(R.dimen.search_item_small_text_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular))
                )
            )
        }
    }
}
