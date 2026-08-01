package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieAward
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding

private const val WreathFallback =
    "https://createstir.b-cdn.net/stir-marketplace/film-festivals/wreath.png"

private val AwardCardShape = RoundedCornerShape(16.dp)
private val AwardCardWidth = 176.dp
private val AccentGold = Color(0xFFFFC15E)

/** Awards strip — polished festival cards with TV focus. */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
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
                listOfNotNull(
                    first.year.takeIf { it.isNotBlank() },
                    items.mapNotNull { it.category.takeIf { c -> c.isNotBlank() } }
                        .distinct()
                        .joinToString(", ")
                        .takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                first.logoUrl?.takeIf { it.isNotBlank() } ?: WreathFallback,
            )
        }

    Column(modifier = modifier.padding(top = 2.dp)) {
        MovieDetailSectionTitle(text = "Awards & Festivals")
        LazyRow(
            modifier = Modifier
                .padding(top = 16.dp)
                .focusRestorer(),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(grouped, key = { it.first }) { (name, detail, logo) ->
                var isFocused by remember { mutableStateOf(false) }

                val scaleAnimationSpec = if (isFocused) {
                    spring<Float>(
                        dampingRatio = 0.85f,
                        stiffness = 180f
                    )
                } else {
                    tween<Float>(durationMillis = 650, easing = LinearOutSlowInEasing)
                }

                val animatedScale by animateFloatAsState(
                    targetValue = if (isFocused) 1.10f else 1.0f,
                    animationSpec = scaleAnimationSpec,
                    label = "AwardCardScale"
                )

                Surface(
                    onClick = {},
                    shape = ClickableSurfaceDefaults.shape(AwardCardShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            shape = AwardCardShape,
                        ),
                        focusedBorder = Border(
                            border = BorderStroke(1.2.dp, AccentGold.copy(alpha = 0.88f)),
                            shape = AwardCardShape,
                        ),
                    ),
                    glow = ClickableSurfaceDefaults.glow(
                        focusedGlow = Glow(
                            elevationColor = Color.White.copy(alpha = 0.50f),
                            elevation = 20.dp,
                        ),
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .width(AwardCardWidth)
                        .height(176.dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                        .zIndex(if (isFocused) 10f else 1f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1A1A1C),
                                        Color(0xFF0C0C0E),
                                    ),
                                ),
                            ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(BrewImageUrl.withDimensions(logo, 96, 64, quality = "100"))
                                        .size(48, 48)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(40.dp),
                                )
                            }

                            Text(
                                text = name,
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp,
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 10.dp),
                            )

                            if (detail.isNotBlank()) {
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 14.sp,
                                    ),
                                    color = AccentGold.copy(alpha = 0.78f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth(0.55f)
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                AccentGold.copy(alpha = 0.5f),
                                                Color.Transparent,
                                            ),
                                        ),
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}
