package com.example.playlistmaker.media.ui.compose

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.view_model.ManagePlaylistViewModel

@Composable
fun ManagePlaylistScreen(
    navController: NavController,
    viewModel: ManagePlaylistViewModel
) {

    val context = LocalContext.current
    var isShowDialog by remember { mutableStateOf(false) }

    // Отслеживаем основной объект со статусом экрана создания плейлиста
    val managePlaylistState = viewModel.managePlaylistState.collectAsState().value

    // Отслеживаем строку с текстом перед закрытием экрана
    val finishManagePlaylistState = viewModel.finishManagePlaylistState.collectAsState().value

    if (finishManagePlaylistState.isNotEmpty()) {
        Toast.makeText(context, finishManagePlaylistState, Toast.LENGTH_LONG).show()
        navController.navigateUp()
    }

    // Перехватываем системную кнопку Назад для показа диалога, если есть не сохранённые данные
    BackHandler(managePlaylistState.isFilled) {
        isShowDialog = true
    }

    if (isShowDialog) {
        ExitWithoutSaveDialog(
            onExitConfirmed = {
                isShowDialog = false
                navController.navigateUp()
            },
            onCancelDialog = { isShowDialog = false })
    }

    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.setImageUri(uri)
    }

    val screenTitle = when (managePlaylistState.isNewPlaylist) {
        true -> stringResource(R.string.new_playlist)
        false -> stringResource(R.string.edit_playlist_title)
    }

    val buttonTitle = when (managePlaylistState.isNewPlaylist) {
        true -> stringResource(R.string.create)
        false -> stringResource(R.string.save)
    }

    Column(Modifier
        .fillMaxSize()
        .background(colorResource(R.color.main_background))) {
        // TopBar
        ControlledTopBar(
            title = screenTitle,
            isFilled = managePlaylistState.isFilled,
            onNavigateBack = { navController.navigateUp() })
        // Контейнер обложки Альбома
        Box(
            modifier = Modifier
                .background(colorResource(R.color.main_background))
                .fillMaxWidth()
                .weight(0.55F)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AlbumImage(managePlaylistState.playlistImagePath) {
                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }
        }
        // Контейнер полей ввода
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.30F)
                .padding(horizontal = 16.dp)
                .background(colorResource(R.color.main_background))
        ) {
            CommonTextField(
                hint = stringResource(R.string.name_hint),
                text = managePlaylistState.playlistTitle,
                onTextChange = { newTitle ->
                    viewModel.titleChanged(newTitle)
                })
            Spacer(Modifier.height(12.dp))
            CommonTextField(
                hint = stringResource(R.string.description),
                text = managePlaylistState.playlistDescription,
                onTextChange = { newDescription ->
                    viewModel.descriptionChanged(newDescription)
                })
        }
        // Контейнер кнопки ввода
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.15F)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            contentAlignment = Alignment.BottomCenter) {
            CommonButton(buttonTitle, managePlaylistState.isSavable) {
                viewModel.savePlaylistData()
            }
        }
    }
}

@Composable
fun ExitWithoutSaveDialog(onExitConfirmed: () -> Unit, onCancelDialog: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.playlist_creation_cancelling_confirmation))
        },
        text = {
            Text(text = stringResource(R.string.filled_data_lost_warning))
        },
        onDismissRequest = onCancelDialog,
        dismissButton = {
            TextButton(
                onClick = onCancelDialog
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onExitConfirmed()
                }
            ) {
                Text(stringResource(R.string.finish))
            }
        }
    )
}

@Composable
fun CommonButton(title: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.light_blue),
            disabledContainerColor = colorResource(R.color.light_gray)),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        enabled = enabled,
        content = {
            Text(
                text = title,
                maxLines = 1,
                style = TextStyle(
                    color = colorResource(R.color.white),
                    fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_medium))
                )
            ) }
    )
}

@Composable
fun CommonTextField(hint: String, text: String, onTextChange: (newText: String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = text,
        singleLine = true,
        onValueChange = { onTextChange(it) },
        label = {
            Text(hint,
                fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_regular))
            )
        },
        textStyle = TextStyle(
            fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_regular))
        ),
        colors = TextFieldDefaults.colors(
            focusedLabelColor = colorResource(R.color.light_blue),
            focusedContainerColor = colorResource(R.color.main_background),
            focusedTextColor = colorResource(R.color.main_foreground),
            focusedIndicatorColor = colorResource(R.color.light_blue),
            unfocusedLabelColor = colorResource(R.color.main_foreground),
            unfocusedContainerColor = colorResource(R.color.main_background),
            unfocusedTextColor = colorResource(R.color.main_foreground),
            unfocusedIndicatorColor = colorResource(R.color.text_label),
        ),
    )
}

@Composable
fun AlbumImage(playlistImagePath: String?, onClick: () -> Unit) {
    if (playlistImagePath.isNullOrEmpty()) {
        val color = colorResource(R.color.light_gray)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(colorResource(R.color.main_background))
                .fillMaxHeight()
                .aspectRatio(1F)
                .drawBehind {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(120f, 120f), 100f)
                    drawRoundRect(
                        color = color,
                        style = Stroke(width = 1.dp.toPx(), pathEffect = pathEffect),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
                .clickable(onClick = onClick),
            ) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = stringResource(R.string.media_empty_message),
                tint = color,
                modifier = Modifier.scale(4.16F)
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlistImagePath)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlledTopBar(title: String, isFilled: Boolean, onNavigateBack: () -> Unit) {
    var isShowDialog by remember { mutableStateOf(false) }

    if (isShowDialog)
        ExitWithoutSaveDialog(
            onExitConfirmed = {
                isShowDialog = false
                onNavigateBack()
            },
            onCancelDialog = { isShowDialog = false }
        )

    Row(modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Box(
            modifier = Modifier.width(56.dp).fillMaxHeight()
        ) {
            IconButton(
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    when (isFilled) {
                        true -> isShowDialog = true
                        false -> onNavigateBack()
                    }
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description",
                    tint = colorResource(R.color.main_foreground)
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = colorResource(R.color.main_foreground),
                    fontSize = dimensionResource(R.dimen.title_font_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_medium))
                )
            )
        }
    }
}