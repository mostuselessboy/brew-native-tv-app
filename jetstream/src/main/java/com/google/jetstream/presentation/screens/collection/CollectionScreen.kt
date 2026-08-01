package com.google.jetstream.presentation.screens.collection

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.CollectionSectionDetails
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.common.BrewLandscapeCardWidth
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.common.BrewMovieCardStyle
import com.google.jetstream.presentation.common.CollectionShimmerSkeleton
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.screens.movies.MovieDetailBackButton
import com.google.jetstream.presentation.theme.BrewDisplay
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.theme.JetStreamBottomListPadding
import kotlinx.coroutines.delay

object CollectionScreen {
    const val SectionIdBundleKey = "sectionId"
}

private val CollectionBackdropHeight = 400.dp
private val CollectionCardGap = 18.dp
private val CollectionTrayTopPadding = 12.dp
private val CollectionTrayMinHeight = BrewLandscapeCardWidth * 9f / 16f * 1.12f

@Composable
fun CollectionScreen(
    onBackPressed: () -> Unit,
    onMovieSelected: (Movie) -> Unit,
    viewModel: CollectionScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        CollectionScreenUiState.Loading -> CollectionShimmerSkeleton(modifier = Modifier.fillMaxSize())
        CollectionScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
        is CollectionScreenUiState.Done -> CollectionContent(
            details = state.details,
            restoreMovieId = viewModel.restoreMovieId,
            onBackPressed = {
                viewModel.clearRestoreTarget()
                onBackPressed()
            },
            onMovieSelected = { movie ->
                viewModel.rememberFocusedMovie(movie.id)
                onMovieSelected(movie)
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CollectionContent(
    details: CollectionSectionDetails,
    restoreMovieId: String?,
    onBackPressed: () -> Unit,
    onMovieSelected: (Movie) -> Unit,
) {
    val childPadding = rememberChildPadding()
    val firstCardFocusRequester = remember { FocusRequester() }
    val restoreCardFocusRequester = remember { FocusRequester() }
    val rowState = rememberLazyListState()
    val initialFocusedIndex = restoreMovieId?.let { id ->
        details.movies.indexOfFirst { it.id == id }.takeIf { it >= 0 }
    } ?: 0
    var focusedMovieIndex by remember(details.id, restoreMovieId) {
        mutableIntStateOf(initialFocusedIndex)
    }
    val focusedMovie = details.movies.getOrNull(focusedMovieIndex)
    val heroArt = focusedMovie?.heroBackdropUri?.takeIf { it.isNotBlank() }
        ?: focusedMovie?.posterUri
        ?: ""
    val trayEntryFocusRequester = if (
        restoreMovieId != null && details.movies.any { it.id == restoreMovieId }
    ) {
        restoreCardFocusRequester
    } else {
        firstCardFocusRequester
    }
    var didInitialFocus by remember(details.id) { mutableStateOf(false) }
    var restoredMovieId by remember(details.id) { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBackPressed)

    LaunchedEffect(details.id, details.movies.size, restoreMovieId) {
        if (details.movies.isEmpty()) return@LaunchedEffect

        val targetId = restoreMovieId
        if (targetId != null && restoredMovieId != targetId) {
            val index = details.movies.indexOfFirst { it.id == targetId }
            if (index >= 0) {
                focusedMovieIndex = index
                rowState.scrollToItem(index)
                delay(160)
                runCatching { restoreCardFocusRequester.requestFocus() }
                restoredMovieId = targetId
            }
            return@LaunchedEffect
        }

        if (!didInitialFocus) {
            delay(120)
            runCatching { firstCardFocusRequester.requestFocus() }
            didInitialFocus = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        CollectionBackdropImage(
            artUri = heroArt,
            modifier = Modifier
                .fillMaxWidth()
                .height(CollectionBackdropHeight)
                .align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = JetStreamBottomListPadding),
            verticalArrangement = Arrangement.Bottom,
        ) {
            CollectionHeroText(
                details = details,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )

            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(CollectionCardGap),
                contentPadding = PaddingValues(
                    start = childPadding.start,
                    end = childPadding.end,
                    top = CollectionTrayTopPadding,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CollectionTrayMinHeight)
                    .focusGroup()
                    .focusRestorer { trayEntryFocusRequester },
            ) {
                itemsIndexed(details.movies, key = { _, movie -> movie.id }) { index, movie ->
                    val focusModifier = when {
                        movie.id == restoreMovieId -> Modifier.focusRequester(restoreCardFocusRequester)
                        index == 0 -> Modifier.focusRequester(firstCardFocusRequester)
                        else -> Modifier
                    }
                    BrewLandscapeMovieCard(
                        movie = movie,
                        onClick = { onMovieSelected(movie) },
                        style = BrewMovieCardStyle.Tray,
                        onFocused = { focusedMovieIndex = index },
                        modifier = focusModifier,
                    )
                }
            }
        }

        MovieDetailBackButton(
            onBackPressed = onBackPressed,
            downFocusRequester = trayEntryFocusRequester,
            rightFocusRequester = trayEntryFocusRequester,
        )
    }
}

@Composable
private fun CollectionBackdropImage(
    artUri: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    AnimatedContent(
        targetState = artUri,
        transitionSpec = {
            fadeIn(tween(320)).togetherWith(fadeOut(tween(220)))
        },
        label = "collectionBackdrop",
        modifier = modifier,
    ) { uri ->
        if (uri.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(BrewImageUrl.forCollectionHero(uri))
                    .size(
                        BrewImageUrl.COLLECTION_HERO_WIDTH,
                        BrewImageUrl.COLLECTION_HERO_HEIGHT,
                    )
                    .crossfade(280)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.05f to Color.Transparent,
                                    0.45f to Color.Black.copy(alpha = 0.35f),
                                    0.72f to Color.Black.copy(alpha = 0.72f),
                                    1f to Color.Black,
                                ),
                            ),
                        )
                        drawRect(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Black.copy(alpha = 0.65f),
                                    0.45f to Color.Black.copy(alpha = 0.22f),
                                    1f to Color.Transparent,
                                ),
                            ),
                        )
                    },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111)),
            )
        }
    }
}

@Composable
private fun CollectionHeroText(
    details: CollectionSectionDetails,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()

    Column(
        modifier = modifier
            .padding(
                start = childPadding.start,
                end = childPadding.end,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Curated by Brew",
            color = Color.White.copy(alpha = 0.94f),
            fontFamily = BrewDisplay,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            letterSpacing = (-0.2).sp,
        )
        Text(
            text = details.title,
            color = Color(0xFFFFC15E),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = (-2).sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
        details.subheading?.takeIf { it.isNotBlank() }?.let { subheading ->
            Text(
                text = subheading,
                color = Color.White.copy(alpha = if (subheading.length > 72) 0.58f else 0.96f),
                fontFamily = BrewDisplay,
                fontStyle = FontStyle.Italic,
                fontSize = if (subheading.length > 72) 14.sp else 22.sp,
                lineHeight = if (subheading.length > 72) 20.sp else 28.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp),
            )
        }
        Text(
            text = "SHOWING ${details.movies.size} OF ${details.total} TITLES",
            color = Color.White.copy(alpha = 0.94f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
            ),
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
