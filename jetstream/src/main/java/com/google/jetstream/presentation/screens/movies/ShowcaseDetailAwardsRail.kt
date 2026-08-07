package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.runtime.Composable
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

/** Endless looping basicMarquee for all movie awards with subtle edge gradient overlays. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowcaseDetailAwardsRail(
    awards: List<MovieAward>,
    modifier: Modifier = Modifier,
) {
    if (awards.isEmpty()) return

    val context = LocalContext.current
    val wreathRequest = remember(context) {
        ImageRequest.Builder(context)
            .data(WreathUrl)
            .size(56, 56)
            .crossfade(false)
            .build()
    }

    // Multiply list so basicMarquee has seamless continuous looping content
    val marqueeAwards = remember(awards) {
        if (awards.size < 6) awards + awards + awards + awards
        else awards + awards
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    velocity = 45.dp,
                    initialDelayMillis = 0,
                ),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            marqueeAwards.forEach { award ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AsyncImage(
                        model = wreathRequest,
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
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }

        // Edge scrims — left/right fades
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(56.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black,
                            0.35f to Color.Black.copy(alpha = 0.85f),
                            0.7f to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(36.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                    ),
                ),
        )
    }
}
