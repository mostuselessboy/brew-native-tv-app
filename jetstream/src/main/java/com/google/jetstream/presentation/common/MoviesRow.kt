package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
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
    subheading: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    showItemTitle: Boolean = true,
    showIndexOverImage: Boolean = false,
    onMovieSelected: (movie: Movie) -> Unit = {},
    firstItemFocusRequester: FocusRequester? = null,
    upFocusRequester: FocusRequester? = null,
    leftFocusRequester: FocusRequester? = null,
    restoreFocusMovieId: String? = null,
    onRestoreFocusComplete: () -> Unit = {},
    onFirstItemFocused: (() -> Unit)? = null,
    focusEnabled: Boolean = true,
) {
    val (lazyRow, defaultFirstItem) = remember { FocusRequester.createRefs() }
    val firstItem = firstItemFocusRequester ?: defaultFirstItem
    val restoreFocusRequester = remember { FocusRequester() }
    val cardHeight = BrewLandscapeCardWidth * 9f / 16f

    LaunchedEffect(restoreFocusMovieId, movieList) {
        val targetId = restoreFocusMovieId ?: return@LaunchedEffect
        if (movieList.none { it.id == targetId }) return@LaunchedEffect
        delay(160)
        if (runCatching { restoreFocusRequester.requestFocus() }.isSuccess) {
            onRestoreFocusComplete()
        }
    }

    Column(modifier = modifier.focusGroup()) {
        if (title != null) {
            Text(
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = startPadding,
                    top = 0.dp,
                    bottom = 4.dp,
                ),
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                contentPadding = PaddingValues(
                    start = startPadding + if (showIndexOverImage) RankedRowExtraStartPadding else 0.dp,
                    end = endPadding,
                    top = 2.dp,
                    bottom = 2.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    if (showIndexOverImage) RankedCardSpacing else 10.dp,
                ),
                modifier = Modifier
                    .graphicsLayer { clip = false }
                    .focusRequester(lazyRow)
                    .then(
                        if (focusEnabled) {
                            Modifier.focusRestorer { firstItem }
                        } else {
                            Modifier.focusProperties { canFocus = false }
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(
                    movieList,
                    key = { _, movie -> movie.id },
                    contentType = { _, _ -> "movie_card" },
                ) { index, movie ->
                    val itemModifier = when {
                        restoreFocusMovieId == movie.id ->
                            Modifier.focusRequester(restoreFocusRequester)
                        index == 0 ->
                            Modifier.focusRequester(firstItem)
                        else ->
                            Modifier
                    }
                    val focusModifier = itemModifier.focusProperties {
                        left = when {
                            index > 0 -> FocusRequester.Default
                            leftFocusRequester != null -> leftFocusRequester
                            else -> FocusRequester.Cancel
                        }
                        if (upFocusRequester != null) {
                            up = upFocusRequester
                        }
                    }

                    if (showIndexOverImage) {
                        RankedMovieItem(
                            rank = index + 1,
                            movie = movie,
                            cardHeight = cardHeight,
                            showTitle = showItemTitle,
                            focusEnabled = focusEnabled,
                            onClick = {
                                runCatching { lazyRow.saveFocusedChild() }
                                onMovieSelected(movie)
                            },
                            onFocused = {
                                if (index == 0) onFirstItemFocused?.invoke()
                            },
                            modifier = focusModifier,
                        )
                    } else {
                        BrewLandscapeMovieCard(
                            movie = movie,
                            showTitle = showItemTitle,
                            focusEnabled = focusEnabled,
                            onFocused = {
                                if (index == 0) onFirstItemFocused?.invoke()
                            },
                            onClick = {
                                runCatching { lazyRow.saveFocusedChild() }
                                onMovieSelected(movie)
                            },
                            modifier = focusModifier,
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

private val RankedRowExtraStartPadding = 24.dp
private val RankedCardSpacing = 22.dp
private val RankedNumberLeadingInset = (-14).dp
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
    focusEnabled: Boolean,
    onClick: () -> Unit,
    onFocused: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isDoubleDigit = rank > 9
    val numberWidth = if (isDoubleDigit) 72.dp else 54.dp

    Box(
        modifier = Modifier
            .height(cardHeight + 8.dp)
            .padding(start = if (isDoubleDigit) 16.dp else 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = rank.toString(),
            color = Color.White.copy(alpha = if (rank == 1) 0.96f else 0.68f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = if (isDoubleDigit) 88.sp else 96.sp,
            letterSpacing = (-4).sp,
            lineHeight = if (isDoubleDigit) 88.sp else 96.sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = RankedNumberLeadingInset)
                .widthIn(min = numberWidth)
                .zIndex(0f),
        )
        BrewLandscapeMovieCard(
            movie = movie,
            showTitle = showTitle,
            focusEnabled = focusEnabled,
            onFocused = onFocused,
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = numberWidth * 0.48f)
                .zIndex(1f)
                .then(modifier),
        )
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
