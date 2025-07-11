package com.example.playlistmaker.util.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R

@Composable
fun PlaceholderProgressBar() {
    TopMargin(R.dimen.progressbar_top_margin)
    CircularProgressIndicator(
        modifier = Modifier.width(dimensionResource(R.dimen.progressbar_diameter)),
        color = colorResource(R.color.light_blue)
    )
}

@Composable
fun Placeholder(
    message: String,
    topMarginRes: Int = R.dimen.placeholder_top_margin,
    imageRes: Int = R.drawable.nothing_found) {
    TopMargin(topMarginRes)
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null
    )
    TopMargin(R.dimen.media_margin)
    Text(text = message,
        textAlign = TextAlign.Center,
        style = TextStyle(
            color = colorResource(R.color.main_foreground),
            fontSize = dimensionResource(R.dimen.title_font_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_medium))
        )
    )
}

@Composable
fun TopMargin(sizeRes: Int) {
    Spacer(modifier = Modifier.height(dimensionResource(sizeRes)))
}

@Composable
fun PlaceholderButton(title: String, onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        modifier = Modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.main_foreground)),
        onClick = onClick,
        content = {
            Text(
                text = title,
                maxLines = 1,
                style = TextStyle(
                    color = colorResource(R.color.main_background),
                    fontSize = dimensionResource(R.dimen.refresh_button_text_size).value.sp,
                    fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
                    fontFamily = FontFamily(Font(R.font.ys_display_medium))
                )
            ) }
    )
}

