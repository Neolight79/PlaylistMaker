package com.example.playlistmaker.media.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.compose.CommonTitleBar
import kotlinx.coroutines.launch

@Composable
fun MediaScreen(
    navController: NavController
) {

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CommonTitleBar(stringResource(R.string.mediateka))
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = colorResource(R.color.main_background),
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .padding(horizontal = 16.dp),
                        color = colorResource(R.color.main_foreground)
                    )
                }
            },
            divider = { }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                text = { TabText(stringResource(R.string.favorite_tracks)) }
            )

            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                text = { TabText(stringResource(R.string.playlists)) }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when(page) {
                0 -> FavoriteScreen(navController = navController)
                1 -> PlaylistsScreen(navController = navController)
            }
        }
    }
}

@Composable
fun TabText(text: String) {
    Text(text = text,
        style = TextStyle(
            color = colorResource(R.color.main_foreground),
            fontSize = dimensionResource(R.dimen.artist_name_font_size).value.sp,
            fontWeight = FontWeight(dimensionResource(R.dimen.title_font_weight).value.toInt()),
            fontFamily = FontFamily(Font(R.font.ys_display_medium))
        )
    )
}
