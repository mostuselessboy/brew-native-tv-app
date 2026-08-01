package com.google.jetstream.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.screens.movies.MovieDetailTokens
import com.google.jetstream.presentation.theme.BrewTitle

/** Matches vod-frontend landscape tray card — compact for TV shelves. */
val BrewLandscapeCardWidth = 220.dp
private val BrewDetailCardWidth = 280.dp
private val BrewTrayCardShape = RoundedCornerShape(9.dp)
private val BrewDetailCardShape = RoundedCornerShape(10.dp)
private val BrewDetailShellBorder = Color.White.copy(alpha = 0.1f)

/**
 * Brew.tv landscape card — title + sales pitch overlay ON the art
 * (vod-frontend `MovieCardInfoOverlay`), not below the card.
 */
@Composable
fun BrewLandscapeMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = null,
    fillAvailableWidth: Boolean = false,
    showTitle: Boolean = true,
    style: BrewMovieCardStyle = BrewMovieCardStyle.Tray,
    onFocused: () -> Unit = {},
) {
    val context = LocalContext.current
    val imageRequest = remember(movie.id, movie.posterUri) {
        ImageRequest.Builder(context)
            .data(BrewImageUrl.forCard(movie.posterUri))
            .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
            .crossfade(false)
            .build()
    }
    val cardGradient = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.42f to Color.Transparent,
                0.7f to Color.Black.copy(alpha = 0.42f),
                1f to Color.Black.copy(alpha = 0.96f),
            ),
        )
    }

    val shape = when (style) {
        BrewMovieCardStyle.Tray -> BrewTrayCardShape
        BrewMovieCardStyle.Detail -> BrewDetailCardShape
    }
    val titleSize = when (style) {
        BrewMovieCardStyle.Tray -> 16.sp
        BrewMovieCardStyle.Detail -> 14.sp
    }
    val titleLine = when (style) {
        BrewMovieCardStyle.Tray -> 17.sp
        BrewMovieCardStyle.Detail -> 15.sp
    }
    val containerColor = when (style) {
        BrewMovieCardStyle.Tray -> Color.Black
        BrewMovieCardStyle.Detail -> Color.White.copy(alpha = 0.06f)
    }

    var isFocused by remember { mutableStateOf(false) }

    val targetScale = if (isFocused) {
        when (style) {
            BrewMovieCardStyle.Tray -> 1.15f
            BrewMovieCardStyle.Detail -> 1.12f
        }
    } else {
        1.0f
    }

    val scaleAnimationSpec = if (isFocused) {
        spring<Float>(
            dampingRatio = 0.76f,
            stiffness = 380f
        )
    } else {
        tween<Float>(durationMillis = 500, easing = LinearOutSlowInEasing)
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = scaleAnimationSpec,
        label = "MovieCardScale"
    )

    val widthModifier = when {
        fillAvailableWidth -> Modifier.fillMaxWidth()
        cardWidth != null -> Modifier.width(cardWidth)
        style == BrewMovieCardStyle.Detail -> Modifier.width(BrewDetailCardWidth)
        else -> Modifier.width(BrewLandscapeCardWidth)
    }

    val focusedBorder = Border(
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)),
        shape = shape,
    )
    val surfaceBorder = when (style) {
        BrewMovieCardStyle.Detail -> ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, BrewDetailShellBorder),
                shape = shape,
            ),
            focusedBorder = focusedBorder,
        )
        BrewMovieCardStyle.Tray -> ClickableSurfaceDefaults.border(
            focusedBorder = focusedBorder,
        )
    }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        border = surfaceBorder,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = containerColor,
            focusedContainerColor = containerColor,
        ),
        modifier = modifier
            .then(widthModifier)
            .aspectRatio(16f / 9f)
            .onFocusChanged { 
                isFocused = it.isFocused
                if (it.isFocused) onFocused() 
            }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .zIndex(if (isFocused) 10f else 1f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Color.Black),
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cardGradient),
            )

            MovieBadgeChrome(movie = movie, compact = true)

            movie.watchProgressPercent?.takeIf { it > 0 }?.let { progress ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.14f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress / 100f)
                            .height(4.dp)
                            .background(MovieDetailTokens.AccentYellow),
                    )
                }
            }

            if (showTitle) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Text(
                        text = movie.name,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleSize,
                        lineHeight = titleLine,
                        letterSpacing = (-0.5).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val meta = movieCardMetaLine(movie)
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            color = Color.White.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                lineHeight = 11.sp,
                                letterSpacing = (-0.35).sp,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Legacy wrapper kept for immersive/grid call sites. */
@Composable
fun MovieCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    image: @Composable BoxScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(BrewTrayCardShape),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                shape = BrewTrayCardShape,
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        modifier = modifier,
        content = {
            Box { image() }
        },
    )
    title()
}
