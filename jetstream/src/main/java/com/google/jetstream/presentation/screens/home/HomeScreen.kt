package com.google.jetstream.presentation.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.HomeShimmerSkeleton

import com.google.jetstream.presentation.screens.splash.BrewSplashScreen

@Composable
fun HomeScreen(
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    onViewMoreClick: (sectionId: String) -> Unit = {},
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    page: String = BrewPages.HOME,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(key = page),
    showcaseFocusRequester: FocusRequester? = null,
    firstRowFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    onShowcaseOpenMovie: () -> Unit = {},
    isTabVisible: Boolean = true,
    onMovieFocused: (sectionId: String, movieId: String) -> Unit = { _, _ -> },
    lastFocusedSectionId: String? = null,
    lastFocusedMovieId: String? = null,
) {
    SideEffect { homeScreeViewModel.setPage(page) }

    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = homeScreeViewModel.peekInitialState(page),
    )

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            val continueWatchingState by homeScreeViewModel.continueWatchingState
                .collectAsStateWithLifecycle()
            if (isTabVisible) {
                Catalog(
                    sections = s.sections,
                    continueWatchingState = continueWatchingState,
                    onMovieClick = onMovieClick,
                    goToVideoPlayer = goToVideoPlayer,
                    onViewMoreClick = onViewMoreClick,
                    onShowcaseOpenMovie = onShowcaseOpenMovie,
                    showcaseFocusRequester = showcaseFocusRequester,
                    firstRowFocusRequester = firstRowFocusRequester,
                    sidebarFocusRequester = sidebarFocusRequester,
                    onMovieFocused = onMovieFocused,
                    lastFocusedSectionId = lastFocusedSectionId,
                    lastFocusedMovieId = lastFocusedMovieId,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        is HomeScreenUiState.Loading -> {
            if (isTabVisible) {
                HomeShimmerSkeleton(modifier = Modifier.fillMaxSize())
            }
        }
        is HomeScreenUiState.Error -> {
            if (isTabVisible) {
                Error(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
