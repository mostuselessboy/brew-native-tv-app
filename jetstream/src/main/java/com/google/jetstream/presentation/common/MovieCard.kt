package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.theme.BrewTitle

/** Matches vod-frontend landscape tray card — compact for TV shelves. */
val BrewLandscapeCardWidth = 220.dp
private val BrewCardShape = RoundedCornerShape(9.dp)

@Composable
fun BrewLandscapeMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = BrewLandscapeCardWidth,
    showTitle: Boolean = true,
    onFocused: () -> Unit = {},
    focusEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val imageUri = movie.backdropUri?.takeIf { it.isNotBlank() } ?: movie.posterUri
    val imageRequest = remember(movie.id, imageUri) {
        ImageRequest.Builder(context)
            .data(BrewImageUrl.forCard(imageUri))
            .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
            .crossfade(false)
            .build()
    }
    val widthModifier = if (cardWidth != null) {
        Modifier.width(cardWidth)
    } else {
        Modifier.fillMaxWidth()
    }

    if (!focusEnabled) {
        Box(modifier = modifier.then(widthModifier)) {
            BrewLandscapeCardContent(
                imageRequest = imageRequest,
                movie = movie,
                showTitle = showTitle,
            )
        }
        return
    }

    BrewFocusedCardFrame(
        onClick = onClick,
        onFocused = onFocused,
        modifier = modifier.then(widthModifier),
        shape = BrewCardShape,
    ) {
        BrewLandscapeCardContent(
            imageRequest = imageRequest,
            movie = movie,
            showTitle = showTitle,
        )
    }
}

@Composable
private fun BrewLandscapeCardContent(
    imageRequest: ImageRequest,
    movie: Movie,
    showTitle: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(BrewCardShape)
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
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.42f to Color.Transparent,
                            0.7f to Color.Black.copy(alpha = 0.42f),
                            1f to Color.Black.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )

        MovieBadgeChrome(movie = movie, compact = true)

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
                    fontSize = 16.sp,
                    lineHeight = 17.sp,
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

/** Legacy wrapper kept for immersive/grid call sites. */
@Composable
fun MovieCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    image: @Composable BoxScope.() -> Unit,
) {
    BrewFocusedCardFrame(onClick = onClick, modifier = modifier) {
        Box(content = image)
    }
    title()
}
