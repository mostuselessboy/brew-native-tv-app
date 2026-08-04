package com.google.jetstream.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.common.BrewFeedbackToast
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.HomeShimmerSkeleton

@Composable
fun HomeScreen(
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    onMoreInfoClick: (movie: Movie) -> Unit = {},
    onSignInRequired: () -> Unit = {},
    onViewMoreClick: (sectionId: String) -> Unit = {},
    onTrayMovieOpen: () -> Unit = {},
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    page: String = BrewPages.HOME,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(key = page),
    showcaseFocusRequester: FocusRequester? = null,
    firstRowFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    showcaseSlideIndex: Int = 0,
    onShowcaseSlideChange: (Int) -> Unit = {},
    onShowcaseOpenMovie: () -> Unit = {},
    isTabVisible: Boolean = true,
    onMovieFocused: (sectionId: String, movieId: String) -> Unit = { _, _ -> },
    lastFocusedSectionId: String? = null,
    lastFocusedMovieId: String? = null,
) {
    LaunchedEffect(page) { homeScreeViewModel.setPage(page) }

    LaunchedEffect(isTabVisible) {
        if (isTabVisible) {
            homeScreeViewModel.refreshContinueWatchingIfPending()
        }
    }

    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = homeScreeViewModel.peekInitialState(page),
    )

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            val continueWatchingState by homeScreeViewModel.continueWatchingState
                .collectAsStateWithLifecycle()
            val showcaseAccess by homeScreeViewModel.showcaseAccess
                .collectAsStateWithLifecycle()
            val optimisticReminderIds by homeScreeViewModel.optimisticReminderIds
                .collectAsStateWithLifecycle()
            val reminderFeedback by homeScreeViewModel.reminderFeedback
                .collectAsStateWithLifecycle()

            Box(modifier = Modifier.fillMaxSize()) {
            if (isTabVisible) {
                Catalog(
                    sections = s.sections,
                    continueWatchingState = continueWatchingState,
                    onMovieClick = onMovieClick,
                    goToVideoPlayer = goToVideoPlayer,
                    onMoreInfoClick = onMoreInfoClick,
                    onToggleReminder = { movie ->
                        homeScreeViewModel.toggleReminder(movie, onSignInRequired)
                    },
                    onViewMoreClick = onViewMoreClick,
                    onTrayMovieOpen = onTrayMovieOpen,
                    onShowcaseOpenMovie = onShowcaseOpenMovie,
                    showcaseAccess = showcaseAccess,
                    optimisticReminderIds = optimisticReminderIds,
                    isReminderSet = homeScreeViewModel::isReminderSet,
                    showcaseFocusRequester = showcaseFocusRequester,
                    firstRowFocusRequester = firstRowFocusRequester,
                    sidebarFocusRequester = sidebarFocusRequester,
                    showcaseSlideIndex = showcaseSlideIndex,
                    onShowcaseSlideChange = onShowcaseSlideChange,
                    onMovieFocused = onMovieFocused,
                    lastFocusedSectionId = lastFocusedSectionId,
                    lastFocusedMovieId = lastFocusedMovieId,
                    modifier = Modifier.fillMaxSize(),
                )
            }
                BrewFeedbackToast(
                    message = reminderFeedback,
                    onDismiss = homeScreeViewModel::dismissReminderFeedback,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
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
