package com.google.jetstream.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
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
private val CatalogSectionSpacing = 20.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Catalog(
    sections: List<HomeSection>,
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    showcaseFocusRequester: FocusRequester? = null,
    firstRowFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    restoreFocusMovieId: String? = null,
    onRestoreFocusComplete: () -> Unit = {},
    isTabVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val coroutineScope = rememberCoroutineScope()
    val localFirstRowFocus = remember { FocusRequester() }
    val localShowcasePrimaryFocus = remember { FocusRequester() }
    val showcaseSecondaryFocus = remember { FocusRequester() }
    val firstRowFocus = firstRowFocusRequester ?: localFirstRowFocus
    val showcasePrimaryFocus = showcaseFocusRequester ?: localShowcasePrimaryFocus

    val showcaseSection = remember(sections) {
        sections.firstOrNull { it.type == HomeSectionType.Showcase }
    }
    val scrollSections = remember(sections) {
        sections.filter { it.type != HomeSectionType.Showcase }
    }

    val listState = rememberLazyListState()
    val hasShowcase = showcaseSection != null
    val showcaseListOffset = if (hasShowcase) 1 else 0

    val firstContentRowIndex = remember(scrollSections) {
        scrollSections.indexOfFirst {
            it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive
        }.coerceAtLeast(0)
    }

    val scrollToTop: () -> Unit = {
        coroutineScope.launch {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(isTabVisible, restoreFocusMovieId, scrollSections) {
        if (!isTabVisible || restoreFocusMovieId == null) return@LaunchedEffect
        val sectionIndex = scrollSections.indexOfFirst { section ->
            section.movies.any { it.id == restoreFocusMovieId }
        }
        if (sectionIndex >= 0) {
            listState.scrollToItem(showcaseListOffset + sectionIndex)
        }
    }

    val showTopScrim by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 8
        }
    }

    Box(modifier = modifier.background(ScreenBlack)) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(CatalogSectionSpacing),
            contentPadding = PaddingValues(top = 8.dp, bottom = 56.dp),
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = isTabVisible },
        ) {
            if (showcaseSection != null) {
                item(key = "showcase") {
                    FeaturedMoviesCarousel(
                        movies = showcaseSection.movies,
                        padding = childPadding,
                        onMovieClick = onMovieClick,
                        goToVideoPlayer = goToVideoPlayer,
                        primaryFocusRequester = if (isTabVisible) showcasePrimaryFocus else null,
                        secondaryFocusRequester = if (isTabVisible) showcaseSecondaryFocus else null,
                        downFocusRequester = if (isTabVisible) firstRowFocus else null,
                        sidebarFocusRequester = if (isTabVisible) sidebarFocusRequester else null,
                        focusEnabled = isTabVisible,
                        onShowcaseFocused = scrollToTop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ShowcaseHeight)
                            .focusGroup(),
                    )
                }
            }

            itemsIndexed(
                items = scrollSections,
                key = { _, section -> section.id },
                contentType = { _, section -> section.type.name },
            ) { index, section ->
                when (section.type) {
                    HomeSectionType.RandomMoviePicker -> {
                        RandomMoviePickerSection(
                            movies = section.movies,
                            onWatchNow = onMovieClick,
                            modifier = Modifier.focusGroup(),
                        )
                    }

                    HomeSectionType.Immersive,
                    HomeSectionType.Row -> {
                        val isFirstContentRow = index == firstContentRowIndex
                        MoviesRow(
                            modifier = Modifier
                                .focusGroup()
                                .bringIntoViewIfChildrenAreFocused(
                                    PaddingValues(top = 64.dp, bottom = 48.dp),
                                )
                                .padding(bottom = 4.dp),
                            movieList = section.movies,
                            title = section.title,
                            subheading = section.subheading,
                            titleStyle = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.15).sp,
                            ),
                            itemDirection = ItemDirection.Horizontal,
                            showIndexOverImage = section.showRanking ||
                                section.type == HomeSectionType.Immersive,
                            onMovieSelected = onMovieClick,
                            firstItemFocusRequester = if (isFirstContentRow && isTabVisible) {
                                firstRowFocus
                            } else {
                                null
                            },
                            upFocusRequester = if (isFirstContentRow && isTabVisible) {
                                showcaseSecondaryFocus
                            } else {
                                null
                            },
                            leftFocusRequester = if (isFirstContentRow && isTabVisible) {
                                sidebarFocusRequester
                            } else {
                                null
                            },
                            restoreFocusMovieId = restoreFocusMovieId,
                            onRestoreFocusComplete = onRestoreFocusComplete,
                            onFirstItemFocused = if (isFirstContentRow && isTabVisible) {
                                {
                                    coroutineScope.launch {
                                        listState.scrollToItem(
                                            showcaseListOffset + firstContentRowIndex,
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                            focusEnabled = isTabVisible,
                        )
                    }

                    HomeSectionType.Showcase -> Unit
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
