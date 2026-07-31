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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.zIndex
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
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.common.PrimeTabReveal
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
    openCollectionScreen: (sectionId: String) -> Unit,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openVideoPlayer: (Movie) -> Unit,
    openVideoPlayerById: (movieId: String) -> Unit,
    openSignInPhone: () -> Unit = {},
    openSignInEmail: () -> Unit = {},
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
    val profileContentFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val libraryContentFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val searchContentFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    var didStartupFocus by rememberSaveable { mutableStateOf(false) }

    val contentFocusRequester = when (tabRoute) {
        Screens.Home(),
        Screens.BrewPlus(),
        Screens.Shorts(),
        Screens.Store(),
        -> homeShowcaseFocusRequester
        Screens.Profile() -> profileContentFocusRequester
        Screens.Favourites() -> libraryContentFocusRequester
        Screens.Search() -> searchContentFocusRequester
        else -> null
    }

    LaunchedEffect(Unit) {
        if (!didStartupFocus) {
            runCatching { homeShowcaseFocusRequester.requestFocus() }
            didStartupFocus = true
        }
    }

    LaunchedEffect(isComingBackFromDifferentScreen) {
        if (!isComingBackFromDifferentScreen) return@LaunchedEffect
        kotlinx.coroutines.delay(120)
        when (dashboardViewModel.focusRestoreTarget) {
            CatalogFocusRestoreTarget.ShowcasePrimary ->
                runCatching { homeShowcaseFocusRequester.requestFocus() }
            CatalogFocusRestoreTarget.FirstTray ->
                runCatching { homeFirstTrayFocusRequester.requestFocus() }
        }
        resetIsComingBackFromDifferentScreen()
    }

    LaunchedEffect(Unit) {
        dashboardViewModel.openPlayer.collect { slug ->
            openVideoPlayerById(slug)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    val unusedOpenCategory = openCategoryMovieList

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
            onRailFocus = { screen, selected ->
                dashboardViewModel.onRailItemFocused(screen, selected, ::navigateToTab)
            },
            onRailBlur = dashboardViewModel::onRailItemUnfocused,
            contentFocusRequester = contentFocusRequester,
            sidebarFocusRequester = sidebarFocusRequester,
            modifier = Modifier.fillMaxSize().background(Color.Black),
        ) {
            Body(
                tabTarget = tabTarget,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openCollectionScreen = openCollectionScreen,
                onPlayMovie = dashboardViewModel::playMovie,
                onPlayLibraryItem = { item ->
                    dashboardViewModel.playLibraryItem(item, openMovieDetailsScreen)
                },
                openSignInPhone = openSignInPhone,
                openSignInEmail = openSignInEmail,
                onBrowseHome = { navigateToTab(Screens.Home) },
                onBrowseStore = { navigateToTab(Screens.Store) },
                homeShowcaseFocusRequester = homeShowcaseFocusRequester,
                homeFirstTrayFocusRequester = homeFirstTrayFocusRequester,
                sidebarFocusRequester = sidebarFocusRequester,
                profileContentFocusRequester = profileContentFocusRequester,
                libraryContentFocusRequester = libraryContentFocusRequester,
                searchContentFocusRequester = searchContentFocusRequester,
                activeTabRoute = tabTarget.route,
                onOpenMovieFromTray = {
                    dashboardViewModel.rememberFocusTarget(CatalogFocusRestoreTarget.FirstTray)
                },
                onOpenMovieFromShowcase = {
                    dashboardViewModel.rememberFocusTarget(CatalogFocusRestoreTarget.ShowcasePrimary)
                },
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

private val NonCatalogTabRoutes = listOf(
    Screens.Profile(),
    Screens.Favourites(),
    Screens.Search(),
)

@Composable
private fun Body(
    tabTarget: TabNavTarget,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openCollectionScreen: (sectionId: String) -> Unit,
    onPlayMovie: (Movie) -> Unit,
    onPlayLibraryItem: (LibraryItem) -> Unit,
    openSignInPhone: () -> Unit,
    openSignInEmail: () -> Unit,
    onBrowseHome: () -> Unit,
    onBrowseStore: () -> Unit,
    homeShowcaseFocusRequester: FocusRequester,
    homeFirstTrayFocusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester,
    profileContentFocusRequester: FocusRequester,
    libraryContentFocusRequester: FocusRequester,
    searchContentFocusRequester: FocusRequester,
    activeTabRoute: String,
    onOpenMovieFromTray: () -> Unit,
    onOpenMovieFromShowcase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = tabTarget.route
    var visitedRoutes by remember { mutableStateOf(setOf(Screens.Home())) }
    LaunchedEffect(route) {
        if (route !in visitedRoutes) {
            visitedRoutes = visitedRoutes + route
        }
    }

    Box(modifier = modifier.clipToBounds()) {
        CatalogTabRoutes.forEach { catalogRoute ->
            if (catalogRoute !in visitedRoutes) return@forEach
            val page = catalogPageForRoute(catalogRoute) ?: return@forEach
            val visible = route == catalogRoute
            androidx.compose.runtime.key(catalogRoute) {
                PrimeTabReveal(
                    active = visible,
                    modifier = Modifier.zIndex(if (visible) 1f else 0f),
                ) {
                    HomeScreen(
                        page = page,
                        onMovieClick = {
                            onOpenMovieFromTray()
                            openMovieDetailsScreen(it.id)
                        },
                        onViewMoreClick = openCollectionScreen,
                        onShowcaseOpenMovie = onOpenMovieFromShowcase,
                        goToVideoPlayer = onPlayMovie,
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

        NonCatalogTabRoutes.forEach { nonCatalogRoute ->
            if (nonCatalogRoute !in visitedRoutes) return@forEach
            val visible = route == nonCatalogRoute
            androidx.compose.runtime.key(nonCatalogRoute) {
                PrimeTabReveal(
                    
                    active = visible,
                    modifier = Modifier.zIndex(if (visible) 1f else 0f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusProperties { canFocus = visible },
                    ) {
                        NonCatalogTab(
                        route = nonCatalogRoute,
                        isTabVisible = visible,
                        openMovieDetailsScreen = openMovieDetailsScreen,
                        onPlayLibraryItem = onPlayLibraryItem,
                        openSignInPhone = openSignInPhone,
                        openSignInEmail = openSignInEmail,
                        onBrowseHome = onBrowseHome,
                        onBrowseStore = onBrowseStore,
                        sidebarFocusRequester = sidebarFocusRequester,
                        profileContentFocusRequester = profileContentFocusRequester,
                        libraryContentFocusRequester = libraryContentFocusRequester,
                        searchContentFocusRequester = searchContentFocusRequester,
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun NonCatalogTab(
    route: String,
    isTabVisible: Boolean,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    onPlayLibraryItem: (LibraryItem) -> Unit,
    openSignInPhone: () -> Unit,
    openSignInEmail: () -> Unit,
    onBrowseHome: () -> Unit,
    onBrowseStore: () -> Unit,
    sidebarFocusRequester: FocusRequester,
    profileContentFocusRequester: FocusRequester,
    libraryContentFocusRequester: FocusRequester,
    searchContentFocusRequester: FocusRequester,
) {
    when (route) {
        Screens.Profile() -> ProfileScreen(
            openSignInPhone = openSignInPhone,
            openSignInEmail = openSignInEmail,
            sidebarFocusRequester = sidebarFocusRequester,
            contentFocusRequester = profileContentFocusRequester,
            isTabVisible = isTabVisible,
        )
        Screens.Favourites() -> FavouritesScreen(
            onMovieClick = openMovieDetailsScreen,
            onLibraryItemClick = onPlayLibraryItem,
            onScroll = { },
            isTopBarVisible = true,
            onSignInClick = openSignInPhone,
            onBrowseHome = onBrowseHome,
            onBrowseStore = onBrowseStore,
            sidebarFocusRequester = sidebarFocusRequester,
            contentFocusRequester = libraryContentFocusRequester,
            isTabVisible = isTabVisible,
        )
        Screens.Search() -> SearchScreen(
            onMovieClick = { openMovieDetailsScreen(it.id) },
            onScroll = { },
            sidebarFocusRequester = sidebarFocusRequester,
            contentFocusRequester = searchContentFocusRequester,
            isTabVisible = isTabVisible,
        )
        else -> ProfileScreen(
            openSignInPhone = openSignInPhone,
            openSignInEmail = openSignInEmail,
            sidebarFocusRequester = sidebarFocusRequester,
            contentFocusRequester = profileContentFocusRequester,
            isTabVisible = isTabVisible,
        )
    }
}
