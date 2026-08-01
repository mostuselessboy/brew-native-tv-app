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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.presentation.common.ContinueWatchingTraySkeleton
import com.google.jetstream.presentation.common.ItemDirection
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.common.ShowcaseHeight
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused

private val ScreenBlack = Color(0xFF000000)

private sealed interface CatalogListItem {
    data class Section(val section: HomeSection, val sectionIndex: Int) : CatalogListItem
    data object ContinueWatching : CatalogListItem
}

private fun buildCatalogItems(
    sections: List<HomeSection>,
    showContinueWatching: Boolean,
): List<CatalogListItem> {
    if (!showContinueWatching) {
        return sections.mapIndexed { index, section ->
            CatalogListItem.Section(section, index)
        }
    }
    val firstRowIndex = sections.indexOfFirst {
        it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive
    }
    // 2nd tray: after showcase + first content row (mobile parity).
    val insertBeforeIndex = when {
        firstRowIndex >= 0 -> firstRowIndex + 1
        else -> sections.indexOfFirst { it.type == HomeSectionType.Showcase } + 1
    }.coerceAtLeast(0)
    return buildList {
        sections.forEachIndexed { index, section ->
            if (index == insertBeforeIndex) {
                add(CatalogListItem.ContinueWatching)
            }
            add(CatalogListItem.Section(section, index))
        }
        if (insertBeforeIndex >= sections.size) {
            add(CatalogListItem.ContinueWatching)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Catalog(
    sections: List<HomeSection>,
    continueWatchingState: ContinueWatchingTrayState = ContinueWatchingTrayState.Hidden,
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    onViewMoreClick: (sectionId: String) -> Unit = {},
    onTrayMovieOpen: () -> Unit = {},
    onShowcaseOpenMovie: () -> Unit = {},
    showcaseFocusRequester: FocusRequester? = null,
    firstRowFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    showcaseSlideIndex: Int = 0,
    onShowcaseSlideChange: (Int) -> Unit = {},
    onMovieFocused: (sectionId: String, movieId: String) -> Unit = { _, _ -> },
    lastFocusedSectionId: String? = null,
    lastFocusedMovieId: String? = null,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val localFirstRowFocus = remember { FocusRequester() }
    val localShowcasePrimaryFocus = remember { FocusRequester() }
    val showcaseSecondaryFocus = remember { FocusRequester() }
    val continueWatchingFocus = remember { FocusRequester() }
    val firstRowFocus = firstRowFocusRequester ?: localFirstRowFocus
    val showcasePrimaryFocus = showcaseFocusRequester ?: localShowcasePrimaryFocus
    val listState = rememberLazyListState()
    val showTopScrim by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 12
        }
    }
    val showContinueWatchingSlot = continueWatchingState !is ContinueWatchingTrayState.Hidden
    val catalogItems = remember(sections, showContinueWatchingSlot) {
        buildCatalogItems(sections, showContinueWatchingSlot)
    }
    val firstContentSectionIndex = remember(sections) {
        sections.indexOfFirst {
            it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive
        }
    }

    Box(modifier = modifier.background(ScreenBlack)) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 56.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = catalogItems,
                key = { item ->
                    when (item) {
                        CatalogListItem.ContinueWatching -> "continue_watching"
                        is CatalogListItem.Section -> item.section.id
                    }
                },
                contentType = { item ->
                    when (item) {
                        CatalogListItem.ContinueWatching -> "continue_watching"
                        is CatalogListItem.Section -> item.section.type.name
                    }
                },
            ) { item ->
                when (item) {
                    CatalogListItem.ContinueWatching -> {
                        when (continueWatchingState) {
                            ContinueWatchingTrayState.Hidden -> Unit
                            ContinueWatchingTrayState.Loading -> {
                                ContinueWatchingTraySkeleton(
                                    modifier = Modifier.focusGroup(),
                                )
                            }
                            is ContinueWatchingTrayState.Ready -> {
                                MoviesRow(
                                    modifier = Modifier.focusGroup(),
                                    movieList = continueWatchingState.movies,
                                    title = "Continue Watching",
                                    subtitle = "Pick up where you left off",
                                    itemDirection = ItemDirection.Horizontal,
                                    showItemTitle = true,
                                    onMovieSelected = { movie ->
                                        if (movie.libraryClickAction == com.google.jetstream.data.util.LibraryClickAction.Play) {
                                            goToVideoPlayer(movie)
                                        } else {
                                            onTrayMovieOpen()
                                            onMovieClick(movie)
                                        }
                                    },
                                    onMovieFocused = { movie ->
                                        onMovieFocused("continue_watching", movie.id)
                                    },
                                    lastFocusedMovieId = if (lastFocusedSectionId == "continue_watching") lastFocusedMovieId else null,
                                    firstItemFocusRequester = continueWatchingFocus,
                                )
                            }
                        }
                    }

                    is CatalogListItem.Section -> {
                        val section = item.section
                        val sectionIndex = item.sectionIndex
                        when (section.type) {
                            HomeSectionType.Showcase -> {
                                FeaturedMoviesCarousel(
                                    movies = section.movies,
                                    padding = childPadding,
                                    initialSlideIndex = showcaseSlideIndex,
                                    onSlideIndexChange = onShowcaseSlideChange,
                                    onMovieClick = {
                                        onShowcaseOpenMovie()
                                        onMovieClick(it)
                                    },
                                    goToVideoPlayer = goToVideoPlayer,
                                    primaryFocusRequester = showcasePrimaryFocus,
                                    secondaryFocusRequester = showcaseSecondaryFocus,
                                    sidebarFocusRequester = sidebarFocusRequester,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(ShowcaseHeight)
                                        .focusGroup()
                                        .bringIntoViewIfChildrenAreFocused(),
                                )
                            }

                            HomeSectionType.RandomMoviePicker -> Unit

                            HomeSectionType.Immersive,
                            HomeSectionType.Row -> {
                                val isFirstContentRow = sectionIndex == firstContentSectionIndex
                                MoviesRow(
                                    modifier = Modifier.focusGroup(),
                                    movieList = section.movies,
                                    title = section.title,
                                    titleStyle = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = (-0.35).sp,
                                    ),
                                    itemDirection = ItemDirection.Horizontal,
                                    showIndexOverImage = section.showRanking ||
                                        section.type == HomeSectionType.Immersive,
                                    onMovieSelected = { movie ->
                                        onTrayMovieOpen()
                                        onMovieClick(movie)
                                    },
                                    onMovieFocused = { movie ->
                                        onMovieFocused(section.id, movie.id)
                                    },
                                    lastFocusedMovieId = if (lastFocusedSectionId == section.id) lastFocusedMovieId else null,
                                    onViewMoreClick = {
                                        onViewMoreClick(section.slug ?: section.id)
                                    },
                                    firstItemFocusRequester = if (isFirstContentRow) {
                                        firstRowFocus
                                    } else {
                                        null
                                    },
                                )
                            }
                        }
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
