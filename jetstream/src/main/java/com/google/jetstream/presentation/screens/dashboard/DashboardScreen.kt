package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.util.resolveUserAvatarDisplayUrl
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.common.PrimeTabReveal
import com.google.jetstream.presentation.screens.favourites.FavouritesScreen
import com.google.jetstream.presentation.screens.home.HomeScreen
import com.google.jetstream.presentation.screens.profile.ProfileScreen
import com.google.jetstream.presentation.screens.profile.AccountsSection
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
    val currentUser by dashboardViewModel.currentUser.collectAsStateWithLifecycle()
    val accountAvatarUrl = resolveUserAvatarDisplayUrl(currentUser, 80)
    var tabRoute by rememberSaveable { mutableStateOf(Screens.Home()) }
    var tabSlideDirection by remember { mutableIntStateOf(1) }
    val tabTarget = remember(tabRoute, tabSlideDirection) {
        TabNavTarget(route = tabRoute, slideDirection = tabSlideDirection)
    }
    val catalogFocusHandles = remember {
        CatalogTabRoutes.associateWith { CatalogTabFocusHandles() }
    }
    val sidebarFocusRequester: FocusRequester? = null
    val profileContentFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val libraryContentFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val searchContentFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
    var didStartupFocus by rememberSaveable { mutableStateOf(false) }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val minSplashMs = 500L
        val maxSplashMs = 2500L
        val start = System.currentTimeMillis()
        kotlinx.coroutines.delay(minSplashMs)
        while (System.currentTimeMillis() - start < maxSplashMs) {
            if (dashboardViewModel.isHomeCatalogCached()) break
            kotlinx.coroutines.delay(50)
        }
        showSplash = false
    }

    val contentFocusRequester = when (tabRoute) {
        Screens.Home(),
        Screens.BrewPlus(),
        Screens.Shorts(),
        Screens.Store(),
        -> catalogFocusHandles[tabRoute]?.showcase
        Screens.Account() -> profileContentFocusRequester
        Screens.Profile() -> profileContentFocusRequester
        Screens.Favourites() -> libraryContentFocusRequester
        Screens.Search() -> searchContentFocusRequester
        else -> null
    }

    LaunchedEffect(Unit) {
        if (!didStartupFocus) {
            catalogFocusHandles[Screens.Home()]?.showcase?.let { requester ->
                runCatching { requester.requestFocus() }
            }
            didStartupFocus = true
        }
    }

    LaunchedEffect(isComingBackFromDifferentScreen) {
        if (!isComingBackFromDifferentScreen) return@LaunchedEffect
        // Wait for movie-detail pop transition before restoring catalog focus.
        kotlinx.coroutines.delay(380)
        val route = tabRoute
        val memory = dashboardViewModel.consumeDetailReturnRestore(route)
        if (memory != null && isCatalogDestination(route)) {
            val handles = catalogFocusHandles[route]
            if (handles != null) {
                when (memory.focusTarget) {
                    CatalogFocusRestoreTarget.ShowcasePrimary ->
                        runCatching { handles.showcase.requestFocus() }
                    CatalogFocusRestoreTarget.FirstTray -> {
                        if (memory.movieId == null) {
                            runCatching { handles.firstTray.requestFocus() }
                        }
                        // When movieId is set, MoviesRow restores the card once via targetItem.
                    }
                }
            }
            if (memory.focusTarget == CatalogFocusRestoreTarget.FirstTray && memory.movieId != null) {
                kotlinx.coroutines.delay(450)
                dashboardViewModel.clearFocusedMovie(route)
            }
        }
        dashboardViewModel.endDetailReturnRestore()
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
        dashboardViewModel.cancelPendingRailNavigation()
        if (tabRoute != route) {
            tabSlideDirection = tabNavTargetFor(route, tabRoute).slideDirection
            tabRoute = route
            dashboardViewModel.onRailTabNavigated(screen)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackPressHandledArea(onBackPressed = onBackPressed) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black)
            ) {
                val topBarTabs = TopBarTabs
                val selectedTabIndex = when {
                    tabRoute == Screens.Search() -> SEARCH_SCREEN_INDEX
                    tabRoute == Screens.Profile() -> PROFILE_SCREEN_INDEX
                    else -> topBarTabs.indexOfFirst { it() == tabRoute }.coerceAtLeast(0)
                }

                Body(
                    tabTarget = tabTarget,
                    dashboardViewModel = dashboardViewModel,
                    catalogFocusHandles = catalogFocusHandles,
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
                    sidebarFocusRequester = sidebarFocusRequester,
                    profileContentFocusRequester = profileContentFocusRequester,
                    libraryContentFocusRequester = libraryContentFocusRequester,
                    searchContentFocusRequester = searchContentFocusRequester,
                    modifier = Modifier.fillMaxSize(),
                )

                DashboardTopBar(
                    selectedTabIndex = selectedTabIndex,
                    contentFocusRequester = contentFocusRequester,
                    avatarUrl = accountAvatarUrl,
                    onScreenSelection = { navigateToTab(it) },
                    modifier = Modifier.zIndex(10f)
                )
            }
        }
        if (showSplash) {
            com.google.jetstream.presentation.screens.splash.BrewSplashScreen(
                modifier = Modifier.fillMaxSize().zIndex(100f)
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

private class CatalogTabFocusHandles {
    val showcase = FocusRequester()
    val firstTray = FocusRequester()
}

private fun catalogPageForRoute(route: String): String? = when (route) {
    Screens.Home() -> BrewPages.HOME
    Screens.BrewPlus() -> BrewPages.BREW_PLUS
    Screens.Shorts() -> BrewPages.SHORTS
    Screens.Store() -> BrewPages.STORE
    else -> null
}

private val NonCatalogTabRoutes = listOf(
    Screens.Account(),
    Screens.Profile(),
    Screens.Favourites(),
    Screens.Search(),
)

@Composable
private fun Body(
    tabTarget: TabNavTarget,
    dashboardViewModel: DashboardViewModel,
    catalogFocusHandles: Map<String, CatalogTabFocusHandles>,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openCollectionScreen: (sectionId: String) -> Unit,
    onPlayMovie: (Movie) -> Unit,
    onPlayLibraryItem: (LibraryItem) -> Unit,
    openSignInPhone: () -> Unit,
    openSignInEmail: () -> Unit,
    onBrowseHome: () -> Unit,
    onBrowseStore: () -> Unit,
    sidebarFocusRequester: FocusRequester?,
    profileContentFocusRequester: FocusRequester,
    libraryContentFocusRequester: FocusRequester,
    searchContentFocusRequester: FocusRequester,
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
            val tabMemory = dashboardViewModel.catalogMemory(catalogRoute)
            val focusHandles = catalogFocusHandles[catalogRoute] ?: return@forEach
            androidx.compose.runtime.key(catalogRoute) {
                PrimeTabReveal(
                    active = visible,
                    modifier = Modifier.zIndex(if (visible) 1f else 0f),
                ) {
                    HomeScreen(
                        page = page,
                        onTrayMovieOpen = {
                            dashboardViewModel.onOpeningMovieDetail(catalogRoute, fromTray = true)
                        },
                        onMovieClick = { movie ->
                            dashboardViewModel.onOpeningMovieDetail(catalogRoute, fromTray = true)
                            openMovieDetailsScreen(movie.id)
                        },
                        onMoreInfoClick = { movie ->
                            dashboardViewModel.onOpeningMovieDetail(catalogRoute, fromTray = false)
                            openMovieDetailsScreen(movie.id)
                        },
                        onSignInRequired = openSignInPhone,
                        onViewMoreClick = openCollectionScreen,
                        onShowcaseOpenMovie = {
                            dashboardViewModel.onOpeningMovieDetail(catalogRoute, fromTray = false)
                        },
                        goToVideoPlayer = onPlayMovie,
                        onScroll = { },
                        isTopBarVisible = true,
                        showcaseFocusRequester = focusHandles.showcase,
                        firstRowFocusRequester = focusHandles.firstTray,
                        sidebarFocusRequester = sidebarFocusRequester,
                        showcaseSlideIndex = tabMemory.showcaseSlideIndex,
                        onShowcaseSlideChange = { index ->
                            dashboardViewModel.rememberShowcaseSlide(catalogRoute, index)
                        },
                        isTabVisible = visible,
                        onMovieFocused = { sectionId, movieId ->
                            dashboardViewModel.saveFocusedItem(catalogRoute, sectionId, movieId)
                        },
                        lastFocusedSectionId = tabMemory.sectionId,
                        lastFocusedMovieId = tabMemory.movieId,
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
                            dashboardViewModel = dashboardViewModel,
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
    sidebarFocusRequester: FocusRequester?,
    profileContentFocusRequester: FocusRequester,
    libraryContentFocusRequester: FocusRequester,
    searchContentFocusRequester: FocusRequester,
    dashboardViewModel: DashboardViewModel,
) {
    when (route) {
        Screens.Account() -> {
            val childPadding = rememberChildPadding()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = childPadding.top)
            ) {
                AccountsSection(
                    onSignInPhone = openSignInPhone,
                    onSignInEmail = openSignInEmail,
                    panelFocusRequester = profileContentFocusRequester,
                )
            }
        }
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
            onMovieClick = { selectedMovie ->
                openMovieDetailsScreen(selectedMovie.id)
            },
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
