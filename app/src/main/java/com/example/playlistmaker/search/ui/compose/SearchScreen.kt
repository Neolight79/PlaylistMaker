package com.example.playlistmaker.search.ui.compose

import android.content.IntentFilter
import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.compose.CommonTitleBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.example.playlistmaker.media.ui.compose.ListItem
import com.example.playlistmaker.util.ui.compose.Placeholder
import com.example.playlistmaker.util.ui.compose.PlaceholderButton
import com.example.playlistmaker.util.ui.compose.PlaceholderProgressBar
import com.example.playlistmaker.search.domain.models.SearchState
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import com.example.playlistmaker.util.LostConnectionBroadcastReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = koinViewModel<SearchViewModel>()
) {

    // Отслеживаем основной объект со статусом экрана избранных треков
    val searchState = viewModel.searchState.collectAsState().value

    // Триггер для выполнения принудительного поиска треков для команды от кнопки повтора попытки
    val mTriggerDirectSearch = MutableStateFlow<Long>(0L)
    val triggerDirectSearch: StateFlow<Long> = mTriggerDirectSearch.asStateFlow()

    val context = LocalContext.current
    val receiver = remember { LostConnectionBroadcastReceiver() }

    DisposableEffect(Unit) {

        context.registerReceiver(receiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LifecycleResumeEffect(Unit) {
        if (searchState is SearchState.TracksHistory)
            viewModel.loadHistory()
        onPauseOrDispose {  }
    }

    // Формируем главную поверхность для макета экрана
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Сверху выводим стандартный заголовок для экрана поиска
        CommonTitleBar(stringResource(R.string.search))
        // Теперь зона с полем ввода
        SearchField(viewModel, triggerDirectSearch)
        // Теперь обработчик стейта от ViewModel
        when (searchState) {
            // Если получаем объект Init, значит ничего показывать не надо
            SearchState.Init -> { }
            // Если получаем объект-индикатор загрузки, то выводим прогрессбар
            SearchState.Loading -> PlaceholderProgressBar()
            // Если получаем сообщение о пустом результате, то плейсхолдер для пустого результата
            is SearchState.Empty -> Placeholder(
                message = searchState.errorMessage,
                topMarginRes = R.dimen.search_placeholder_top_margin,
                imageRes = R.drawable.nothing_found)
            // Если получаем сообщение об ошибке, то плейсхолдер об ошибке с кнопкой повторения запроса
            is SearchState.Error -> {
                Placeholder(
                    message = searchState.message,
                    topMarginRes = R.dimen.search_placeholder_top_margin,
                    imageRes = R.drawable.no_connection)
                PlaceholderButton(stringResource(R.string.refresh)) {
                    // Отправляем в функцию поля ввода команду для повторного запуска поиска
                    mTriggerDirectSearch.value = SystemClock.elapsedRealtime()
                }
            }
            // Если получаем список треков, то выводим его
            is SearchState.TracksFound -> {
                SearchResultsList(
                    foundTracksList = searchState.trackList,
                    onItemClick = { track ->
                        viewModel.addTrack(track)
                        navController.navigate("player/${track.trackId}")
                    }
                )

            }
            // Если получаем список треков истории просмотра, то выводим его
            is SearchState.TracksHistory -> {
                SearchHistoryList(
                    historyTrackList = searchState.trackList,
                    onItemClick = { track ->
                        viewModel.addTrack(track)
                        navController.navigate("player/${track.trackId}")
                                  },
                    onClearHistoryClick = {
                        viewModel.clearHistory()
                        viewModel.loadHistory()
                    })
            }
        }
    }
}

@Composable
fun SearchResultsList(
    foundTracksList: List<Track>,
    onItemClick: (track: Track) -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))
    // Показываем список
    LazyColumn(Modifier.fillMaxSize()) {
        items(foundTracksList) {
            ListItem(it) {
                onItemClick(it)
            }
        }
    }
}

@Composable
fun SearchHistoryList(
    historyTrackList: List<Track>,
    onItemClick: (track: Track) -> Unit,
    onClearHistoryClick: () -> Unit
) {
    // Зазор и заголовок списка просмотренных треков
    Spacer(modifier = Modifier.height(24.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.search_history),
            color = colorResource(R.color.main_foreground),
            fontSize = 19.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_medium)))
    }
    // Показываем список
    LazyColumn(Modifier.fillMaxSize()) {
        items(historyTrackList) {
            ListItem(it) {
                onItemClick(it)
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.settings_row_height)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlaceholderButton(stringResource(R.string.clear_history)) {
                    onClearHistoryClick()
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchField(viewModel: SearchViewModel, triggerDirectSearch: StateFlow<Long>) {

    // Отслеживаем прилет состояния для запуска поиска
    val triggerDirectSearchState = triggerDirectSearch.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)

    ) {
        var text by remember { mutableStateOf("") }
        val interactionSource = remember { MutableInteractionSource() }
        val enabled = true
        val isError = false
        val singleLine = true
        val keyboardController = LocalSoftwareKeyboardController.current

        // Если получили команду запустить поиск, то запускаем его
        var lastDirectSearchState by remember { mutableLongStateOf(0L) }
        if (triggerDirectSearchState != lastDirectSearchState) {
            lastDirectSearchState = triggerDirectSearchState
            viewModel.searchDirectly(text)
        }

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                viewModel.searchDebounce(text)
                            },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .onFocusChanged {
                    if (it.isFocused && text.isEmpty()) viewModel.loadHistory()
                },
            interactionSource = interactionSource,
            enabled = enabled,
            singleLine = singleLine,
            cursorBrush = SolidColor(colorResource(R.color.light_blue)),
            textStyle = TextStyle(
                color = colorResource(R.color.dark),
                fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_regular))
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    viewModel.searchDirectly(text)
                }
            ),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    visualTransformation = VisualTransformation.None,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    prefix = {
                        Icon(
                            modifier = Modifier.offset((-7).dp),
                            tint = colorResource(R.color.glif_gray),
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null)
                    },
                    trailingIcon = {
                        if (text.isNotEmpty())
                            Icon(
                                modifier = Modifier
                                    .offset(4.dp)
                                    .clickable(onClick = {
                                        text = ""
                                        viewModel.clearSearch()
                                    }),
                                tint = colorResource(R.color.glif_gray),
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null)
                                   },
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(start = 20.dp),
                    placeholder = { Text(
                        text = stringResource(R.string.search),
                        style = TextStyle(
                            color = colorResource(R.color.glif_gray),
                            fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                            fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                            fontFamily = FontFamily(Font(R.font.ys_display_regular))
                        )
                    ) },
                    container = {
                        TextFieldDefaults.Container(
                            shape = RoundedCornerShape(8.dp),
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            unfocusedIndicatorLineThickness = 0.dp,
                            focusedIndicatorLineThickness = 0.dp,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                errorContainerColor = colorResource(R.color.edittext_background),
                                focusedContainerColor = colorResource(R.color.edittext_background),
                                disabledContainerColor = colorResource(R.color.edittext_background),
                                unfocusedContainerColor = colorResource(R.color.edittext_background)
                            ),
                        )
                    }
                )
            }
        )
    }
}
