package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.delay

enum class ItemDirection(val aspectRatio: Float) {
    Vertical(2f / 3f),
    Horizontal(16f / 9f);
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoviesRow(
    movieList: MovieList,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Horizontal,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    subtitle: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    showItemTitle: Boolean = true,
    showIndexOverImage: Boolean = false,
    onMovieSelected: (movie: Movie) -> Unit = {},
    onViewMoreClick: (() -> Unit)? = null,
    firstItemFocusRequester: FocusRequester? = null,
    leftFocusRequester: FocusRequester? = null,
    deferCardMount: Boolean = false,
    deferCardMountDelayMs: Long = 280,
) {
    val (lazyRow, defaultFirstItem) = remember { FocusRequester.createRefs() }
    val firstItem = firstItemFocusRequester ?: defaultFirstItem
    val cardHeight = BrewLandscapeCardWidth * 9f / 16f
    var cardsReady by remember(movieList, deferCardMount) {
        mutableStateOf(!deferCardMount)
    }
    LaunchedEffect(movieList, deferCardMount, deferCardMountDelayMs) {
        if (deferCardMount) {
            delay(deferCardMountDelayMs)
            cardsReady = true
        }
    }

    Column(modifier = modifier.focusGroup()) {
        if (title != null) {
            Column(
                modifier = Modifier.padding(
                    start = startPadding,
                    top = 2.dp,
                    bottom = 8.dp,
                ),
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let { sub ->
                    Text(
                        text = sub,
                        color = Color.White.copy(alpha = 0.52f),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                contentPadding = PaddingValues(start = startPadding, end = endPadding),
                horizontalArrangement = Arrangement.spacedBy(
                    if (showIndexOverImage) 8.dp else 10.dp,
                ),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer { firstItem },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(
                    movieList,
                    key = { _, movie -> movie.id },
                    contentType = { _, _ -> "movie_card" },
                ) { index, movie ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    val focusModifier = itemModifier.focusProperties {
                        if (index == 0 && leftFocusRequester != null) {
                            left = leftFocusRequester
                        }
                    }

                    if (!cardsReady) {
                        TrayCardPlaceholder(modifier = focusModifier)
                    } else if (showIndexOverImage) {
                        RankedMovieItem(
                            rank = index + 1,
                            movie = movie,
                            cardHeight = cardHeight,
                            showTitle = showItemTitle,
                            onClick = {
                                lazyRow.saveFocusedChild()
                                onMovieSelected(movie)
                            },
                            modifier = focusModifier,
                        )
                    } else {
                        BrewLandscapeMovieCard(
                            movie = movie,
                            showTitle = showItemTitle,
                            onClick = {
                                lazyRow.saveFocusedChild()
                                onMovieSelected(movie)
                            },
                            modifier = focusModifier,
                        )
                    }
                }

                if (cardsReady && onViewMoreClick != null) {
                    item(key = "view_more") {
                        ViewMoreTrayCard(
                            previewMovies = movieList,
                            onClick = {
                                lazyRow.saveFocusedChild()
                                onViewMoreClick()
                            },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(TrayEdgeFadeWidth)
                    .height(cardHeight)
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to TrayEdgeFadeColor,
                                1f to Color.Transparent,
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(TrayEdgeFadeWidth)
                    .height(cardHeight)
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                1f to TrayEdgeFadeColor,
                            ),
                        ),
                    ),
            )
        }
    }
}

private val TrayCardShape = RoundedCornerShape(9.dp)
private val TrayPlaceholderColor = Color(0xFF141414)

/** Lightweight shelf slot — defers image decode until row is ready. */
@Composable
fun TrayCardPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(BrewLandscapeCardWidth)
            .aspectRatio(16f / 9f)
            .clip(TrayCardShape)
            .background(TrayPlaceholderColor),
    )
}

private val TrayEdgeFadeWidth = 48.dp
private val TrayEdgeFadeColor = Color.Black

/** Prime / Brew Most Watched — giant rank numeral partially behind the card. */
@Composable
private fun RankedMovieItem(
    rank: Int,
    movie: Movie,
    cardHeight: Dp,
    showTitle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDoubleDigit = rank > 9
    val numberWidth = if (isDoubleDigit) 72.dp else 52.dp

    Box(
        modifier = modifier
            .height(cardHeight + 12.dp)
            .padding(start = if (isDoubleDigit) 28.dp else 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = rank.toString(),
            color = Color.White.copy(alpha = 0.92f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = if (isDoubleDigit) 100.sp else 125.sp,
            letterSpacing = (-5).sp,
            lineHeight = if (isDoubleDigit) 100.sp else 125.sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-10).dp)
                .widthIn(min = numberWidth)
                .zIndex(0f),
        )
        BrewLandscapeMovieCard(
            movie = movie,
            showTitle = showTitle,
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(start = numberWidth * 0.55f)
                .zIndex(1f),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ViewMoreTrayCard(
    previewMovies: MovieList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val previewArt = remember(previewMovies.map { it.id }.joinToString()) {
        previewMovies
            .shuffled()
            .mapNotNull { it.posterUri.takeIf { uri -> uri.isNotBlank() } }
            .distinct()
            .take(4)
            .ifEmpty { listOf("") }
    }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(TrayCardShape),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)),
                shape = TrayCardShape,
            ),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF141414),
            focusedContainerColor = Color(0xFF1E1E1E),
        ),
        modifier = modifier
            .width(BrewLandscapeCardWidth)
            .aspectRatio(16f / 9f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    PreviewThumb(url = previewArt.getOrElse(0) { "" }, context = context, modifier = Modifier.weight(1f))
                    PreviewThumb(url = previewArt.getOrElse(1) { "" }, context = context, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.weight(1f)) {
                    PreviewThumb(url = previewArt.getOrElse(2) { "" }, context = context, modifier = Modifier.weight(1f))
                    PreviewThumb(url = previewArt.getOrElse(3) { "" }, context = context, modifier = Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "View More",
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PreviewThumb(
    url: String,
    context: android.content.Context,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A)),
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(BrewImageUrl.forCard(url))
                    .size(BrewImageUrl.CARD_WIDTH / 2, BrewImageUrl.CARD_HEIGHT / 2)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImmersiveListMoviesRow(
    movieList: MovieList,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Horizontal,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    showItemTitle: Boolean = true,
    showIndexOverImage: Boolean = false,
    onMovieSelected: (Movie) -> Unit = {},
    onMovieFocused: (Movie) -> Unit = {},
) {
    MoviesRow(
        movieList = movieList,
        modifier = modifier,
        itemDirection = itemDirection,
        startPadding = startPadding,
        endPadding = endPadding,
        title = title,
        titleStyle = titleStyle,
        showItemTitle = showItemTitle,
        showIndexOverImage = showIndexOverImage,
        onMovieSelected = { movie ->
            onMovieFocused(movie)
            onMovieSelected(movie)
        },
    )
}
