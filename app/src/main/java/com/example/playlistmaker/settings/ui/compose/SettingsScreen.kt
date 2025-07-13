package com.example.playlistmaker.settings.ui.compose

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Switch
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.example.playlistmaker.util.debounceCompose
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()) {

    // Состояние текущего значения системной темы
    val themeSettings = viewModel.themeSettings.collectAsState().value
    // Состояние с текущим вызванным интентом
    val stateIntent = viewModel.stateIntentCompose.collectAsState().value

    // Если необходимо, то вызываем интент
    StartIntent(stateIntent)

    // Формируем главную поверхность для макета экрана
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Сверху выводим стандартный заголовок
        CommonTitleBar(stringResource(R.string.settings))
        // Теперь выводим строки с пунктами настроек по-очереди
        SettingsSwitchRow(
            // Строка с переключателем тёмной и светлой темы
            stringResource(R.string.menu_dark_theme),
            themeSettings.isNightMode) { checked ->
            viewModel.switchTheme(checked)
        }
        SettingsButtonRow(
            // Строка с кнопкой "Поделиться приложением"
            stringResource(R.string.menu_share_app),
            Icons.Filled.Share) {
            viewModel.shareApp()
        }
        SettingsButtonRow(
            // Строка с кнопкой "Обратиться в поддержку"
            stringResource(R.string.menu_support),
            Icons.Filled.SupportAgent) {
            viewModel.mailToSupport()
        }
        SettingsButtonRow(
            // Строка с кнопкой "Пользовательское соглашение"
            stringResource(R.string.menu_user_agreement),
            Icons.Filled.ChevronRight) {
            viewModel.userAgreement()
        }
    }
}

@Composable
fun StartIntent(intent: Intent) {
    var hashCode by remember { mutableIntStateOf(intent.hashCode()) }
    if (intent.hashCode() != hashCode) {
        hashCode = intent.hashCode()
        LocalContext.current.startActivity(intent)
    }
}

// Это общее поле заголовка для всех экранов раздела Media
@Composable
fun CommonTitleBar(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.main_padding))
            .height(dimensionResource(R.dimen.settings_row_height)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = text,
            style = TextStyle(
                color = colorResource(R.color.main_foreground),
                fontSize = dimensionResource(R.dimen.title_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            )
        )
    }
}

// Это строка с заголовком и переключателем
@Composable
fun SettingsSwitchRow(text: String, enabled: Boolean, onClickAction: (checked: Boolean) -> Unit) {
    SettingsRow(text) {
        var checked by remember { mutableStateOf(enabled) }

        Switch(
            checked = checked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(R.color.thumb_checked),
                checkedTrackColor = colorResource(R.color.track_checked),
                uncheckedThumbColor = colorResource(R.color.thumb_unchecked),
                uncheckedTrackColor = colorResource(R.color.track_unchecked),
            ),
            onCheckedChange = {
                checked = it
                onClickAction(checked)
            }
        )
    }
}

// Это строка с заголовком и кнопкой с изображением
@Composable
fun SettingsButtonRow(text: String, icon: ImageVector, onClickAction: () -> Unit) {
    SettingsRow(text) {
        Icon(
            modifier = Modifier.clickable(onClick = debounceCompose(action = onClickAction)),
            imageVector = icon,
            contentDescription = null,
            tint = colorResource(R.color.text_label)
        )
    }
}

// Это заготовка для любой строки
@Composable
fun SettingsRow(text: String, toolElement: @Composable() () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.menu_h_padding),
                vertical = dimensionResource(R.dimen.zero_size)
            )
            .height(dimensionResource(R.dimen.settings_row_height)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text,
            style = TextStyle(
                color = colorResource(R.color.main_foreground),
                fontSize = dimensionResource(R.dimen.row_font_size).value.sp,
                fontWeight = FontWeight(dimensionResource(R.dimen.row_font_weight).value.toInt()),
                fontFamily = FontFamily(Font(R.font.ys_display_regular))
            )
        )
        Spacer(Modifier.weight(1f))
        toolElement()
    }
}