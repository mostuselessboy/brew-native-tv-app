package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieAward
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.common.BrewFocusedCardFrame
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private const val WreathFallback =
    "https://createstir.b-cdn.net/stir-static/wreath.png"

private val AwardCardShape = RoundedCornerShape(12.dp)

/**
 * Awards strip — vod-frontend Awards & Festivals style with wreath fallback.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AwardsFestivalsRow(
    awards: List<MovieAward>,
    modifier: Modifier = Modifier,
) {
    if (awards.isEmpty()) return
    val childPadding = rememberChildPadding()
    val context = LocalContext.current

    val grouped = awards
        .groupBy { it.name }
        .entries
        .map { (name, items) ->
            val first = items.first()
            Triple(
                name,
                items.mapNotNull { it.category.takeIf { c -> c.isNotBlank() } }
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { first.year },
                first.logoUrl?.takeIf { it.isNotBlank() } ?: WreathFallback,
            )
        }

    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            text = "Awards & Festivals",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.padding(start = childPadding.start),
        )
        LazyRow(
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
                top = 14.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            items(grouped, key = { it.first }) { (name, detail, logo) ->
                BrewFocusedCardFrame(
                    onClick = {},
                    shape = AwardCardShape,
                    modifier = Modifier.width(148.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111111), AwardCardShape)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(BrewImageUrl.withDimensions(logo, 96, 96))
                                    .size(96, 96)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(56.dp)
                                    .fillMaxWidth(),
                            )
                            Text(
                                text = name,
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                            if (detail.isNotBlank()) {
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                    }
                }
            }
        }
    }
}
