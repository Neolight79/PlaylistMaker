package com.example.playlistmaker.media.ui.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.models.DialogState
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.ui.view_model.PlaylistViewModel
import com.example.playlistmaker.settings.ui.compose.StartIntent
import com.example.playlistmaker.util.debounceCompose
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    navController: NavController,
    viewModel: PlaylistViewModel
) {

    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    val scope = rememberCoroutineScope()
    val bottomSheetMenuState = rememberModalBottomSheetState()
    val isShowBottomSheetMenu = remember { mutableStateOf(false) }
    val bottomSheetTracksState = rememberBottomSheetScaffoldState()
    val bottomSheetPeekHeight = remember { mutableStateOf(240.dp) }

    // Отслеживаем основное состояние плейлиста
    val playlistState = viewModel.playlistState.collectAsState().value
    // Отслеживаем интент для команды поделиться плейлистом
    val stateIntent = viewModel.stateIntentCompose.collectAsState().value
    // Отслеживаем состояние для показа сообщения и команды выхода из экрана
    val stateFlowMessage = viewModel.stateFlowMessage.collectAsState().value

    // Если необходимо, то вызываем интент для отправки плейлиста
    StartIntent(stateIntent)

    LifecycleResumeEffect(Unit) {
        viewModel.loadPlaylistData()
        onPauseOrDispose {  }
    }

    if (stateFlowMessage.first.isNotEmpty()) {
        Toast.makeText(context, stateFlowMessage.first, Toast.LENGTH_LONG).show()
        if (stateFlowMessage.second) navController.navigateUp()
    }

    ShowDialog(dialogState = dialogState)

    BottomSheetMenu(
        visible = isShowBottomSheetMenu.value,
        playlist = playlistState.first,
        bottomSheetMenuState = bottomSheetMenuState,
        onDismissRequest = {
            scope.launch { bottomSheetMenuState.hide() }.invokeOnCompletion {
                if (!bottomSheetMenuState.isVisible) isShowBottomSheetMenu.value = false
            }
        },
        onSharePlaylist = debounceCompose(action = {
            viewModel.sharePlaylist()
            scope.launch { bottomSheetMenuState.hide() }.invokeOnCompletion {
                if (!bottomSheetMenuState.isVisible) isShowBottomSheetMenu.value = false
            }
        }),
        onModifyPlaylist = debounceCompose(action = {
            navController.navigate("managePlaylist/${playlistState.first.playlistId}")
        }),
        onDeletePlaylist = {
            dialogState = DialogState.Dialog(
                title = context.getString(R.string.playlist_delete_dialog_title),
                message = context.getString(R.string.playlist_delete_dialog_message),
                onConfirm = {
                    viewModel.deletePlaylist()
                },
                onCancel = {
                    dialogState = DialogState.None
                }
            )
        }
    )

    BottomSheetScaffold(
        sheetPeekHeight = bottomSheetPeekHeight.value,
        scaffoldState = bottomSheetTracksState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = colorResource(R.color.main_background),
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .width(50.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(44))
                    .background(colorResource(R.color.handle))
            ) },
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                when (playlistState.second.isEmpty()) {
                    true -> ListItemPlaceholder(stringResource(R.string.playlist_empty_message))
                    false ->
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(playlistState.second) {
                                ListItem(
                                    track = it,
                                    onLongClickAction = {
                                        dialogState = DialogState.Dialog(
                                            title = context.getString(R.string.track_delete_dialog_title),
                                            message = context.getString(R.string.track_delete_dialog_message),
                                            onConfirm = {
                                                viewModel.deleteTrack(it)
                                                dialogState = DialogState.None
                                            },
                                            onCancel = {
                                                dialogState = DialogState.None
                                            }
                                        )
                                    },
                                    onClickAction = { navController.navigate("player/${it.trackId}") }
                                )
                            }
                        }
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.separator_gray)),
            horizontalAlignment = Alignment.Start
        ) {
            // Выводим информацию о плейлисте с кнопкой Назад и кнопками меню и поделиться
            PlaylistInformation(
                playlist = playlistState.first,
                onClickBackButton = { navController.navigateUp() },
                onClickShareButton = debounceCompose(action = { viewModel.sharePlaylist() }),
                onClickMenuButton = {
                    isShowBottomSheetMenu.value = true
                    scope.launch { bottomSheetMenuState.partialExpand() }
                },
                onBottomSpacerSizeChange = { height ->
                    bottomSheetPeekHeight.value = height
                }
            )
        }
    }
}

@Composable
fun ShowDialog(dialogState: DialogState) {
    when (dialogState) {
        is DialogState.None -> {}
        is DialogState.Dialog -> {
            AlertDialog(
                title = {
                    Text(text = dialogState.title)
                },
                text = {
                    Text(text = dialogState.message)
                },
                onDismissRequest = dialogState.onCancel,
                dismissButton = {
                    TextButton(
                        onClick = dialogState.onCancel
                    ) {
                        Text(text = stringResource(R.string.no))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = dialogState.onConfirm
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                }
            )
        }
    }
}

@Composable
fun PlaylistInformation(
    playlist: Playlist,
    onClickBackButton: () -> Unit,
    onClickShareButton: () -> Unit,
    onClickMenuButton: () -> Unit,
    onBottomSpacerSizeChange: (height: Dp) -> Unit
) {
    val density = LocalDensity.current

    // Определяем значения для статистики минут и треков
    val tracksDuration = playlist.playlistTracksDuration / 60000
    val tracksDurationString = when {
        tracksDuration % 10 == 1 && tracksDuration % 100 != 11 ->
            stringResource(R.string.minutes_quantity_1, tracksDuration)
        tracksDuration % 10 in 2..4 && tracksDuration % 100 !in 12..14 ->
            stringResource(R.string.minutes_quantity_2, tracksDuration)
        else ->
            stringResource(R.string.minutes_quantity, tracksDuration)
    }
    val tracksCountString = when {
        playlist.playlistTracksQuantity % 10 == 1 && playlist.playlistTracksQuantity % 100 != 11 ->
            stringResource(R.string.tracks_quantity_1, playlist.playlistTracksQuantity)
        playlist.playlistTracksQuantity % 10 in 2..4 && playlist.playlistTracksQuantity % 100 !in 12..14 ->
            stringResource(R.string.tracks_quantity_2, playlist.playlistTracksQuantity)
        else ->
            stringResource(R.string.tracks_quantity, playlist.playlistTracksQuantity)
    }

    // Первым делом показываем Box, в котором будет еще один Box со стрелкой Назад
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1F)
    ) {
        // Изображение обложки
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
                .fillMaxSize()
                .aspectRatio(1F)
        )
        // Контейнер для кнопки Назад
        Box(
            modifier = Modifier.size(56.dp)
        ) {
            IconButton(
                modifier = Modifier.fillMaxSize(),
                onClick = onClickBackButton) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description",
                    tint = colorResource(R.color.dark)
                )
            }
        }
    }
    // Блок названия и описания плейлиста
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(playlist.playlistName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = colorResource(R.color.dark),
                fontSize = dimensionResource(R.dimen.heavy_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.heavy_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_bold)),
            ))
        Spacer(modifier = Modifier.height(8.dp))
        Text(playlist.playlistDescription,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = colorResource(R.color.dark),
                fontSize = dimensionResource(R.dimen.description_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_regular)),
            ))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.width(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = tracksDurationString,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = colorResource(R.color.dark),
                    fontSize = dimensionResource(R.dimen.description_font_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                ),
                modifier = Modifier.weight(1F)
            )
            Icon(modifier = Modifier.width(13.dp),
                painter = painterResource(id = R.drawable.round_glif),
                tint = colorResource(R.color.dark),
                contentDescription = null
            )
            Text(text = tracksCountString,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = colorResource(R.color.dark),
                    fontSize = dimensionResource(R.dimen.description_font_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                )
            )
        }
        Row(modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.clickable {
                    onClickShareButton()
                },
                imageVector = Icons.Filled.Share,
                contentDescription = "Localized description",
                tint = colorResource(R.color.dark)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                modifier = Modifier.clickable {
                    onClickMenuButton()
                },
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Localized description",
                tint = colorResource(R.color.dark)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                onBottomSpacerSizeChange(with(receiver = density) { it.height.toDp() })
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetMenu(visible: Boolean,
                    playlist: Playlist,
                    bottomSheetMenuState: SheetState,
                    onDismissRequest: () -> Unit,
                    onSharePlaylist: () -> Unit,
                    onModifyPlaylist: () -> Unit,
                    onDeletePlaylist: () -> Unit
) {
    if (visible) {
        val tracksCountString = when {
            playlist.playlistTracksQuantity % 10 == 1 && playlist.playlistTracksQuantity % 100 != 11 ->
                stringResource(R.string.tracks_quantity_1, playlist.playlistTracksQuantity)
            playlist.playlistTracksQuantity % 10 in 2..4 && playlist.playlistTracksQuantity % 100 !in 12..14 ->
                stringResource(R.string.tracks_quantity_2, playlist.playlistTracksQuantity)
            else ->
                stringResource(R.string.tracks_quantity, playlist.playlistTracksQuantity)
        }

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
            sheetState = bottomSheetMenuState) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Здесь выводим заголовок плейлиста с обложкой, а затем пункты меню
                PlaylistContextMenuTopBar(playlist.playlistImagePath, playlist.playlistName, tracksCountString)
                // Здесь выводим пункты меню
                PlaylistContextMenuItem(
                    title = stringResource(R.string.menu_share),
                    onClick = onSharePlaylist)
                PlaylistContextMenuItem(
                    title = stringResource(R.string.menu_edit),
                    onClick = onModifyPlaylist)
                PlaylistContextMenuItem(
                    title = stringResource(R.string.menu_delete),
                    onClick = onDeletePlaylist)
            }
        }
    }
}

@Composable
fun PlaylistContextMenuTopBar(
    playlistImage: String,
    playlistName: String,
    playlistTracksCount: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.settings_row_height))
    ) {
        // Обложка плейлиста
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(66.dp)
                .padding(start = 13.dp, top = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(playlistImage)
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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 14.dp)
        ) {
            // Название плейлиста
            Text(
                text = playlistName,
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
            Row(
                modifier = Modifier
                    .height(13.dp)
                    .width(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playlistTracksCount,
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
            }
        }
    }
}

@Composable
fun PlaylistContextMenuItem(title: String, onClick: () -> Unit) {
    TextButton(
        modifier = Modifier
            .height(dimensionResource(R.dimen.settings_row_height))
            .padding(start = 4.dp),
        onClick = onClick) {
        Text(
            text = title,
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

@Composable
fun ListItemPlaceholder(text: String, fieldHeight: Int = 61) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colorResource(R.color.main_foreground),
            fontSize = dimensionResource(R.dimen.title_font_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_medium)))
    }
}