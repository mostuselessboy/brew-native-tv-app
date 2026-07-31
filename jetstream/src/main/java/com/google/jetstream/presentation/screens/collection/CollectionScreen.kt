package com.google.jetstream.presentation.screens.collection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.common.BrewMovieCardStyle
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.screens.movies.MovieDetailBackButton
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.theme.JetStreamBottomListPadding
import kotlinx.coroutines.delay

object CollectionScreen {
    const val SectionIdBundleKey = "sectionId"
}

private val CollectionHeroHeight = 280.dp

@Composable
fun CollectionScreen(
    onBackPressed: () -> Unit,
    onMovieSelected: (Movie) -> Unit,
    viewModel: CollectionScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        CollectionScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        CollectionScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
        is CollectionScreenUiState.Done -> CollectionContent(
            details = state.details,
            onBackPressed = onBackPressed,
            onMovieSelected = onMovieSelected,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CollectionContent(
    details: CollectionSectionDetails,
    onBackPressed: () -> Unit,
    onMovieSelected: (Movie) -> Unit,
) {
    val childPadding = rememberChildPadding()
    val firstCardFocusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()

    BackHandler(onBack = onBackPressed)

    LaunchedEffect(details.id, details.movies.size) {
        if (details.movies.isNotEmpty()) {
            delay(120)
            firstCardFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CollectionHero(
                details = details,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CollectionHeroHeight),
            )

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = childPadding.start,
                    end = childPadding.end,
                    top = 16.dp,
                    bottom = JetStreamBottomListPadding,
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .focusGroup()
                    .focusRestorer { firstCardFocusRequester },
            ) {
                itemsIndexed(details.movies, key = { _, movie -> movie.id }) { index, movie ->
                    BrewLandscapeMovieCard(
                        movie = movie,
                        onClick = { onMovieSelected(movie) },
                        fillAvailableWidth = true,
                        style = BrewMovieCardStyle.Detail,
                        modifier = if (index == 0) {
                            Modifier.focusRequester(firstCardFocusRequester)
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }

        MovieDetailBackButton(
            onBackPressed = onBackPressed,
            downFocusRequester = firstCardFocusRequester,
            rightFocusRequester = firstCardFocusRequester,
        )
    }
}

@Composable
private fun CollectionHero(
    details: CollectionSectionDetails,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val context = LocalContext.current
    val heroArt = details.heroPosterUri.orEmpty()

    Box(modifier = modifier) {
        if (heroArt.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(BrewImageUrl.forDetail(heroArt))
                    .size(BrewImageUrl.DETAIL_WIDTH, BrewImageUrl.DETAIL_HEIGHT)
                    .crossfade(220)
                    .build(),
                contentDescription = details.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.1f to Color.Transparent,
                                    0.55f to Color.Black.copy(alpha = 0.45f),
                                    1f to Color.Black,
                                ),
                            ),
                        )
                        drawRect(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Black.copy(alpha = 0.7f),
                                    0.5f to Color.Black.copy(alpha = 0.25f),
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = childPadding.start,
                    end = childPadding.end,
                    bottom = 28.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Curated by Brew",
                color = Color.White.copy(alpha = 0.94f),
                fontFamily = BrewTitle,
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
                modifier = Modifier.padding(top = 6.dp),
            )
            details.subheading?.takeIf { it.isNotBlank() }?.let { subheading ->
                Text(
                    text = subheading,
                    color = Color.White.copy(alpha = if (subheading.length > 72) 0.58f else 0.96f),
                    fontFamily = BrewTitle,
                    fontStyle = if (subheading.length > 72) FontStyle.Normal else FontStyle.Italic,
                    fontSize = if (subheading.length > 72) 14.sp else 22.sp,
                    lineHeight = if (subheading.length > 72) 20.sp else 28.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp),
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
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}
