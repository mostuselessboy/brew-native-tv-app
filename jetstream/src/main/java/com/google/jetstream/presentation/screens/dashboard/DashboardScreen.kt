package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.AppSplashGate
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.library.MyLibraryScreen
import com.google.jetstream.presentation.screens.home.HomeScreen
import com.google.jetstream.presentation.screens.profile.ProfileScreen
import com.google.jetstream.presentation.screens.search.SearchScreen
import com.google.jetstream.presentation.utils.Padding
import kotlinx.coroutines.delay

val ParentPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp)

private fun isCatalogDestination(route: String?): Boolean {
    if (route == null) return false
    return route == Screens.Home() ||
        route == Screens.BrewPlus() ||
        route == Screens.Shorts() ||
        route == Screens.Store()
}

@Composable
fun rememberChildPadding(direction: LayoutDirection = LocalLayoutDirection.current): Padding {
    return remember {
        Padding(
            start = ParentPadding.calculateStartPadding(direction) + 8.dp,
            top = ParentPadding.calculateTopPadding(),
            end = ParentPadding.calculateEndPadding(direction) + 8.dp,
            bottom = ParentPadding.calculateBottomPadding(),
        )
    }
}

@Composable
fun DashboardScreen(
    openCategoryMovieList: (categoryId: String) -> Unit,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openVideoPlayer: (Movie) -> Unit,
    openAuth: () -> Unit,
    isComingBackFromDifferentScreen: Boolean,
    resetIsComingBackFromDifferentScreen: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    var tabRoute by rememberSaveable { mutableStateOf(Screens.Home()) }
    val homeShowcaseFocusRequester = remember { FocusRequester() }
    val homeFirstTrayFocusRequester = remember { FocusRequester() }
    val sidebarFocusRequester = remember { FocusRequester() }
    val restoreFocusMovieId by dashboardViewModel.restoreFocusMovieId.collectAsStateWithLifecycle()
    var railFocusEnabled by remember { mutableStateOf(!isCatalogDestination(Screens.Home())) }

    @Suppress("UNUSED_PARAMETER")
    val unusedOpenCategory = openCategoryMovieList
    if (isComingBackFromDifferentScreen) {
        resetIsComingBackFromDifferentScreen()
    }

    LaunchedEffect(tabRoute, restoreFocusMovieId) {
        if (!isCatalogDestination(tabRoute)) {
            railFocusEnabled = true
            AppSplashGate.keepSplashScreen = false
            return@LaunchedEffect
        }
        if (restoreFocusMovieId != null) {
            railFocusEnabled = true
            AppSplashGate.keepSplashScreen = false
            return@LaunchedEffect
        }
        railFocusEnabled = false
        val deadline = System.currentTimeMillis() + 8_000L
        while (System.currentTimeMillis() < deadline) {
            if (runCatching { homeShowcaseFocusRequester.requestFocus() }.isSuccess) {
                railFocusEnabled = true
                AppSplashGate.keepSplashScreen = false
                return@LaunchedEffect
            }
            delay(40)
        }
        railFocusEnabled = true
        AppSplashGate.keepSplashScreen = false
    }

    BackPressHandledArea(onBackPressed = onBackPressed) {
        DashboardNavigationDrawer(
            selectedRoute = tabRoute,
            onNavigateTo = { screen ->
                dashboardViewModel.prefetchCatalog(screen)
                val route = screen()
                if (tabRoute != route) tabRoute = route
            },
            onRailPrefetch = dashboardViewModel::prefetchCatalog,
            contentFocusRequester = if (isCatalogDestination(tabRoute)) {
                homeShowcaseFocusRequester
            } else {
                null
            },
            sidebarFocusRequester = sidebarFocusRequester,
            railFocusEnabled = railFocusEnabled,
            modifier = Modifier.fillMaxSize().background(Color.Black),
        ) {
            Body(
                tabRoute = tabRoute,
                openMovieDetailsScreen = { movieId ->
                    dashboardViewModel.rememberMovieFocus(movieId)
                    openMovieDetailsScreen(movieId)
                },
                openVideoPlayer = openVideoPlayer,
                openAuth = openAuth,
                homeShowcaseFocusRequester = homeShowcaseFocusRequester,
                homeFirstTrayFocusRequester = homeFirstTrayFocusRequester,
                sidebarFocusRequester = sidebarFocusRequester,
                restoreFocusMovieId = restoreFocusMovieId,
                onRestoreFocusComplete = dashboardViewModel::clearRestoreFocusMovieId,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun BackPressHandledArea(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .onPreviewKeyEvent {
            if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                onBackPressed()
                true
            } else {
                false
            }
        }
        .then(modifier),
    content = content,
)

@Composable
private fun Body(
    tabRoute: String,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openVideoPlayer: (Movie) -> Unit,
    openAuth: () -> Unit,
    homeShowcaseFocusRequester: FocusRequester,
    homeFirstTrayFocusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester,
    restoreFocusMovieId: String?,
    onRestoreFocusComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCatalogTab = isCatalogDestination(tabRoute)

    if (isCatalogTab) {
        CatalogTabHost(
            tabRoute = tabRoute,
            modifier = modifier,
        ) { page, isVisible ->
            HomeScreen(
                page = page,
                onMovieClick = { openMovieDetailsScreen(it.id) },
                goToVideoPlayer = openVideoPlayer,
                onScroll = { },
                isTopBarVisible = true,
                showcaseFocusRequester = if (isVisible) homeShowcaseFocusRequester else null,
                firstRowFocusRequester = if (isVisible) homeFirstTrayFocusRequester else null,
                sidebarFocusRequester = if (isVisible) sidebarFocusRequester else null,
                restoreFocusMovieId = if (isVisible) restoreFocusMovieId else null,
                onRestoreFocusComplete = onRestoreFocusComplete,
                isTabVisible = isVisible,
            )
        }
    } else {
        when (tabRoute) {
            Screens.Profile() -> ProfileScreen(openAuth = openAuth)
            Screens.Favourites() -> MyLibraryScreen(
                onMovieClick = openMovieDetailsScreen,
                openAuth = openAuth,
            )
            Screens.Search() -> SearchScreen(
                onMovieClick = { openMovieDetailsScreen(it.id) },
                onScroll = { },
            )
            else -> ProfileScreen(openAuth = openAuth)
        }
    }
}
