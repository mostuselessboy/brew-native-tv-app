package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieAward
import com.google.jetstream.presentation.theme.BrewTitle

private const val WreathUrl =
    "https://createstir.b-cdn.net/stir-marketplace/film-festivals/wreath.png"

/** Endless looping marquee for all movie awards with subtle edge gradient overlays. */
@Composable
fun ShowcaseDetailAwardsRail(
    awards: List<MovieAward>,
    modifier: Modifier = Modifier,
) {
    if (awards.isEmpty()) return

    // Repeat the awards list to ensure we have enough content to fill and scroll continuously
    val repeatedAwards = remember(awards) {
        List(15) { awards }.flatten()
    }

    val scrollState = rememberScrollState()

    // Seamless left-to-right endless scroll loop
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            while (true) {
                scrollState.scrollTo(scrollState.maxValue)
                scrollState.animateScrollTo(
                    value = 0,
                    animationSpec = tween(
                        durationMillis = repeatedAwards.size * 2200,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState, enabled = false),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeatedAwards.forEach { award ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(WreathUrl)
                            .size(60, 60)
                            .build(),
                        contentDescription = award.name,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(28.dp),
                    )
                    Column {
                        Text(
                            text = award.name,
                            color = Color.White,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            letterSpacing = (-0.3).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (award.category.isNotBlank()) {
                            Text(
                                text = award.category,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp), // Space below the title of the award
                            )
                        }
                    }
                }
            }
        }

        // Subtle black gradient overlays on the left and right edges
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(28.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(28.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
        )
    }
}
