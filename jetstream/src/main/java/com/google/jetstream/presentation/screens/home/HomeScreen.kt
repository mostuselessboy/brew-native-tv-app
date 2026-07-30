package com.google.jetstream.presentation.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.AppSplashGate
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.HomeShimmerSkeleton
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    page: String = BrewPages.HOME,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(key = page),
    showcaseFocusRequester: FocusRequester? = null,
    firstRowFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    restoreFocusMovieId: String? = null,
    onRestoreFocusComplete: () -> Unit = {},
    isTabVisible: Boolean = true,
) {
    SideEffect { homeScreeViewModel.setPage(page) }

    LaunchedEffect(Unit) {
        // Never block splash indefinitely if home fails to load.
        kotlinx.coroutines.delay(8_000)
        AppSplashGate.keepSplashScreen = false
    }

    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = homeScreeViewModel.peekInitialState(page),
    )

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            Catalog(
                sections = s.sections,
                onMovieClick = onMovieClick,
                goToVideoPlayer = goToVideoPlayer,
                showcaseFocusRequester = if (isTabVisible) showcaseFocusRequester else null,
                firstRowFocusRequester = if (isTabVisible) firstRowFocusRequester else null,
                sidebarFocusRequester = if (isTabVisible) sidebarFocusRequester else null,
                restoreFocusMovieId = restoreFocusMovieId,
                onRestoreFocusComplete = onRestoreFocusComplete,
                isTabVisible = isTabVisible,
                modifier = Modifier.fillMaxSize(),
            )
        }
        is HomeScreenUiState.Loading -> HomeShimmerSkeleton(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}
