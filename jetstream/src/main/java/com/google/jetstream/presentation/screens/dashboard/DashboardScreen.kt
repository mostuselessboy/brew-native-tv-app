package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
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
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.favourites.FavouritesScreen
import com.google.jetstream.presentation.screens.home.HomeScreen
import com.google.jetstream.presentation.screens.profile.ProfileScreen
import com.google.jetstream.presentation.screens.search.SearchScreen
import com.google.jetstream.presentation.utils.Padding

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
    return androidx.compose.runtime.remember {
        Padding(
            start = ParentPadding.calculateStartPadding(direction) + 8.dp,
            top = ParentPadding.calculateTopPadding(),
            end = ParentPadding.calculateEndPadding(direction) + 8.dp,
            bottom = ParentPadding.calculateBottomPadding()
        )
    }
}

@Composable
fun DashboardScreen(
    openCategoryMovieList: (categoryId: String) -> Unit,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openVideoPlayer: (Movie) -> Unit,
    isComingBackFromDifferentScreen: Boolean,
    resetIsComingBackFromDifferentScreen: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    var tabRoute by rememberSaveable { mutableStateOf(Screens.Home()) }
    var tabSlideDirection by remember { mutableIntStateOf(1) }
    val tabTarget = remember(tabRoute, tabSlideDirection) {
        TabNavTarget(route = tabRoute, slideDirection = tabSlideDirection)
    }
    val homeShowcaseFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val homeFirstTrayFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val sidebarFocusRequester = androidx.compose.runtime.remember { FocusRequester() }

    @Suppress("UNUSED_PARAMETER")
    val unusedOpenCategory = openCategoryMovieList
    if (isComingBackFromDifferentScreen) {
        resetIsComingBackFromDifferentScreen()
    }

    fun navigateToTab(screen: Screens) {
        val route = screen()
        if (tabRoute != route) {
            tabSlideDirection = tabNavTargetFor(route, tabRoute).slideDirection
            tabRoute = route
        }
    }

    BackPressHandledArea(onBackPressed = onBackPressed) {
        DashboardNavigationDrawer(
            selectedRoute = tabRoute,
            onNavigateTo = ::navigateToTab,
            onRailPrefetch = dashboardViewModel::prefetchCatalog,
            onRailPreview = ::navigateToTab,
            contentFocusRequester = if (isCatalogDestination(tabRoute)) {
                homeShowcaseFocusRequester
            } else {
                null
            },
            sidebarFocusRequester = sidebarFocusRequester,
            modifier = Modifier.fillMaxSize().background(Color.Black),
        ) {
            Body(
                tabTarget = tabTarget,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openVideoPlayer = openVideoPlayer,
                homeShowcaseFocusRequester = homeShowcaseFocusRequester,
                homeFirstTrayFocusRequester = homeFirstTrayFocusRequester,
                sidebarFocusRequester = sidebarFocusRequester,
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

private val CatalogTabRoutes = listOf(
    Screens.Home(),
    Screens.BrewPlus(),
    Screens.Shorts(),
    Screens.Store(),
)

private fun catalogPageForRoute(route: String): String? = when (route) {
    Screens.Home() -> BrewPages.HOME
    Screens.BrewPlus() -> BrewPages.BREW_PLUS
    Screens.Shorts() -> BrewPages.SHORTS
    Screens.Store() -> BrewPages.STORE
    else -> null
}

@Composable
private fun Body(
    tabTarget: TabNavTarget,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openVideoPlayer: (Movie) -> Unit,
    homeShowcaseFocusRequester: FocusRequester,
    homeFirstTrayFocusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val route = tabTarget.route
    var visitedRoutes by remember { mutableStateOf(setOf(Screens.Home())) }
    LaunchedEffect(route) {
        if (route !in visitedRoutes) {
            visitedRoutes = visitedRoutes + route
        }
    }
    val isCatalogRoute = catalogPageForRoute(route) != null

    Box(modifier = modifier.clipToBounds()) {
        CatalogTabRoutes.forEach { catalogRoute ->
            if (catalogRoute !in visitedRoutes) return@forEach
            val page = catalogPageForRoute(catalogRoute) ?: return@forEach
            val visible = route == catalogRoute
            androidx.compose.runtime.key(catalogRoute) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (visible) 1f else 0f)
                        .focusProperties { canFocus = visible },
                ) {
                    HomeScreen(
                        page = page,
                        onMovieClick = { openMovieDetailsScreen(it.id) },
                        goToVideoPlayer = openVideoPlayer,
                        onScroll = { },
                        isTopBarVisible = true,
                        showcaseFocusRequester = homeShowcaseFocusRequester,
                        firstRowFocusRequester = homeFirstTrayFocusRequester,
                        sidebarFocusRequester = sidebarFocusRequester,
                        isTabVisible = visible,
                    )
                }
            }
        }

        if (!isCatalogRoute) {
            AnimatedContent(
                targetState = tabTarget,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { dashboardTabTransition() },
                contentKey = { it.route },
                label = "dashboardTabs",
            ) { target ->
                NonCatalogTab(
                    route = target.route,
                    openMovieDetailsScreen = openMovieDetailsScreen,
                    openVideoPlayer = openVideoPlayer,
                )
            }
        }
    }
}

@Composable
private fun NonCatalogTab(
    route: String,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openVideoPlayer: (Movie) -> Unit,
) {
    when (route) {
        Screens.Profile() -> ProfileScreen()
        Screens.Favourites() -> FavouritesScreen(
            onMovieClick = openMovieDetailsScreen,
            onScroll = { },
            isTopBarVisible = true,
        )
        Screens.Search() -> SearchScreen(
            onMovieClick = { openMovieDetailsScreen(it.id) },
            onScroll = { },
        )
        else -> ProfileScreen()
    }
}
