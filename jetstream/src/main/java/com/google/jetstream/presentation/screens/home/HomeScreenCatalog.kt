package com.google.jetstream.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.presentation.common.ItemDirection
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.common.RandomMoviePickerSection
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused
import kotlinx.coroutines.launch

private val ScreenBlack = Color(0xFF000000)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Catalog(
    sections: List<HomeSection>,
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    showcaseFocusRequester: FocusRequester? = null,
    firstRowFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val localFirstRowFocus = remember { FocusRequester() }
    val localShowcasePrimaryFocus = remember { FocusRequester() }
    val showcaseSecondaryFocus = remember { FocusRequester() }
    val firstRowFocus = firstRowFocusRequester ?: localFirstRowFocus
    val showcasePrimaryFocus = showcaseFocusRequester ?: localShowcasePrimaryFocus
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showTopScrim by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 12
        }
    }
    val firstContentRowIndex = remember(sections) {
        sections.indexOfFirst {
            it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive
        }.coerceAtLeast(0)
    }
    val rowOrdinalByIndex = remember(sections) {
        var ordinal = 0
        buildMap {
            sections.forEachIndexed { index, section ->
                if (section.type == HomeSectionType.Row ||
                    section.type == HomeSectionType.Immersive
                ) {
                    put(index, ordinal)
                    ordinal++
                }
            }
        }
    }

    Box(modifier = modifier.background(ScreenBlack)) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 56.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(
                items = sections,
                key = { _, section -> section.id },
                contentType = { _, section -> section.type.name },
            ) { index, section ->
                when (section.type) {
                    HomeSectionType.Showcase -> {
                        FeaturedMoviesCarousel(
                            movies = section.movies,
                            padding = childPadding,
                            onMovieClick = onMovieClick,
                            goToVideoPlayer = goToVideoPlayer,
                            primaryFocusRequester = showcasePrimaryFocus,
                            secondaryFocusRequester = showcaseSecondaryFocus,
                            downFocusRequester = firstRowFocus,
                            sidebarFocusRequester = sidebarFocusRequester,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ShowcaseHeight)
                                .focusGroup()
                                .bringIntoViewIfChildrenAreFocused()
                                .onFocusChanged { state ->
                                    if (!state.hasFocus) return@onFocusChanged
                                    scope.launch {
                                        if (listState.firstVisibleItemIndex == 0 &&
                                            listState.firstVisibleItemScrollOffset > 0
                                        ) {
                                            listState.scrollToItem(0, scrollOffset = 0)
                                        }
                                    }
                                },
                        )
                    }

                    HomeSectionType.RandomMoviePicker -> {
                        RandomMoviePickerSection(
                            movies = section.movies,
                            onSurpriseMe = onMovieClick,
                            modifier = Modifier.focusGroup(),
                        )
                    }

                    HomeSectionType.Immersive,
                    HomeSectionType.Row -> {
                        val rowOrdinal = rowOrdinalByIndex[index] ?: 0
                        val isFirstContentRow = index == firstContentRowIndex
                        val deferCards = rowOrdinal >= 2
                        val deferDelayMs = 200L + ((rowOrdinal - 2).coerceAtLeast(0) * 90L)
                        MoviesRow(
                            modifier = Modifier.focusGroup(),
                            movieList = section.movies,
                            title = section.title,
                            titleStyle = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.15).sp,
                            ),
                            itemDirection = ItemDirection.Horizontal,
                            showIndexOverImage = section.showRanking ||
                                section.type == HomeSectionType.Immersive,
                            onMovieSelected = onMovieClick,
                            deferCardMount = deferCards,
                            deferCardMountDelayMs = deferDelayMs,
                            firstItemFocusRequester = if (isFirstContentRow) {
                                firstRowFocus
                            } else {
                                null
                            },
                            upFocusRequester = if (isFirstContentRow) {
                                showcaseSecondaryFocus
                            } else {
                                null
                            },
                            leftFocusRequester = if (isFirstContentRow) {
                                sidebarFocusRequester
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }

        if (showTopScrim) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Black,
                                0.45f to Color.Black.copy(alpha = 0.75f),
                                1f to Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}
