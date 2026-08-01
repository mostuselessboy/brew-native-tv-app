package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewDisplay
import com.google.jetstream.presentation.theme.BrewTitle

/** Section rail title — mobile-viewer `MOVIE_DETAIL_SECTION_TITLE_CLASSNAME`. */
@Composable
fun MovieDetailSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    useDisplayFont: Boolean = false,
) {
    val padding = rememberChildPadding()
    Text(
        text = text,
        color = MovieDetailTokens.SectionTitleColor,
        fontFamily = if (useDisplayFont) BrewDisplay else BrewTitle,
        fontWeight = FontWeight.Bold,
        fontSize = MovieDetailTokens.SectionTitleSize,
        letterSpacing = (-0.15).sp,
        modifier = modifier.then(Modifier.padding(start = padding.start, top = 12.dp, bottom = 4.dp)),
    )
}
