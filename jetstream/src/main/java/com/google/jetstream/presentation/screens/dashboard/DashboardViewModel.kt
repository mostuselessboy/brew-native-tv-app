package com.google.jetstream.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthRepository
import com.google.jetstream.data.auth.BrewUser
import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.playback.PlaybackLauncher
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.LibraryRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.presentation.screens.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

private const val RailSettleDebounceMs = 320L
private const val RailSettleDebounceCachedMs = 220L

enum class CatalogFocusRestoreTarget {
    ShowcasePrimary,
    FirstTray,
}

/** Per-catalog-tab scroll/focus memory — survives movie-detail navigation. */
data class CatalogTabMemory(
    val focusTarget: CatalogFocusRestoreTarget = CatalogFocusRestoreTarget.ShowcasePrimary,
    val sectionId: String? = null,
    val movieId: String? = null,
    val showcaseSlideIndex: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val libraryRepository: LibraryRepository,
    private val authSessionStore: AuthSessionStore,
    private val authRepository: AuthRepository,
    private val playbackLauncher: PlaybackLauncher,
) : ViewModel() {

    private val _openPlayer = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val openPlayer: SharedFlow<String> = _openPlayer.asSharedFlow()

    val currentUser: StateFlow<BrewUser?> = authSessionStore.currentUser

    private val catalogMemoryByRoute = mutableMapOf<String, CatalogTabMemory>()

    /** Tab that should receive a single focus restore after movie-detail pop. */
    private var pendingDetailReturnRoute: String? = null

    /** Blocks rail debounce navigation while returning from detail (prevents Home flash). */
    private var railNavigationSuppressed = false

    fun catalogMemory(tabRoute: String): CatalogTabMemory =
        catalogMemoryByRoute[tabRoute] ?: CatalogTabMemory()

    private fun updateMemory(tabRoute: String, transform: (CatalogTabMemory) -> CatalogTabMemory) {
        catalogMemoryByRoute[tabRoute] = transform(catalogMemory(tabRoute))
    }

    fun onOpeningMovieDetail(tabRoute: String, fromTray: Boolean) {
        railNavigateJob?.cancel()
        pendingRailScreen = null
        pendingDetailReturnRoute = tabRoute
        railNavigationSuppressed = true
        if (fromTray) {
            updateMemory(tabRoute) {
                it.copy(focusTarget = CatalogFocusRestoreTarget.FirstTray)
            }
        } else {
            updateMemory(tabRoute) {
                it.copy(
                    focusTarget = CatalogFocusRestoreTarget.ShowcasePrimary,
                    sectionId = null,
                    movieId = null,
                )
            }
        }
    }

    fun consumeDetailReturnRestore(tabRoute: String): CatalogTabMemory? {
        if (pendingDetailReturnRoute != tabRoute) return null
        pendingDetailReturnRoute = null
        return catalogMemory(tabRoute)
    }

    fun endDetailReturnRestore() {
        railNavigationSuppressed = false
    }

    fun clearFocusedMovie(tabRoute: String) {
        updateMemory(tabRoute) { it.copy(movieId = null) }
    }

    /** Clears tray scroll memory when switching tabs so content does not steal focus from the rail. */
    fun onRailTabNavigated(screen: Screens) {
        val route = screen()
        if (pendingDetailReturnRoute == route) return
        updateMemory(route) {
            it.copy(sectionId = null, movieId = null)
        }
    }

    fun rememberOpenMovieFromTray(tabRoute: String) {
        onOpeningMovieDetail(tabRoute, fromTray = true)
    }

    fun rememberOpenMovieFromShowcase(tabRoute: String) {
        onOpeningMovieDetail(tabRoute, fromTray = false)
    }

    fun rememberShowcaseSlide(tabRoute: String, index: Int) {
        updateMemory(tabRoute) { it.copy(showcaseSlideIndex = index) }
    }

    fun saveFocusedItem(tabRoute: String, sectionId: String?, movieId: String?) {
        updateMemory(tabRoute) {
            it.copy(sectionId = sectionId, movieId = movieId)
        }
    }

    private var railNavigateJob: Job? = null
    private var pendingRailScreen: Screens? = null

    fun playMovie(movie: Movie) {
        _openPlayer.tryEmit(movie.id)
        viewModelScope.launch {
            playbackLauncher.launchMovie(movie)
        }
    }

    fun playMovieOrNavigate(movie: Movie, navigateToDetails: (String) -> Unit) {
        if (movie.isComingSoon) {
            navigateToDetails(movie.id)
            return
        }
        _openPlayer.tryEmit(movie.id)
        viewModelScope.launch {
            playbackLauncher.launchMovie(movie)
                .onFailure {
                    // Player screen will attempt fallback preparation.
                }
        }
    }

    fun playLibraryItem(item: LibraryItem, openDetail: (String) -> Unit) {
        when (item.clickAction) {
            com.google.jetstream.data.util.LibraryClickAction.Nothing -> return
            com.google.jetstream.data.util.LibraryClickAction.OpenMoviePage -> {
                openDetail(item.movieId)
                return
            }
            com.google.jetstream.data.util.LibraryClickAction.Play -> Unit
        }
        _openPlayer.tryEmit(item.movieId)
        viewModelScope.launch {
            playbackLauncher.launchLibraryItem(item)
                .onFailure { error ->
                    if (error.message == "Open detail") {
                        openDetail(item.movieId)
                    }
                }
        }
    }

    /** Prefetch on IO; navigate only after focus dwells on a non-active tab. */
    fun onRailItemFocused(
        screen: Screens,
        selected: Boolean,
        navigate: (Screens) -> Unit,
    ) {
        prefetchRailScreen(screen)
        if (railNavigationSuppressed) return
        if (selected) {
            pendingRailScreen = null
            railNavigateJob?.cancel()
            return
        }

        pendingRailScreen = screen
        railNavigateJob?.cancel()
        railNavigateJob = viewModelScope.launch(Dispatchers.Default) {
            val page = catalogPageKey(screen)
            val cached = page?.let { movieRepository.peekHomeSections(it) }
            val settleMs = when {
                page == null -> RailSettleDebounceMs
                !cached.isNullOrEmpty() -> RailSettleDebounceCachedMs
                else -> RailSettleDebounceMs
            }
            delay(settleMs)
            if (pendingRailScreen != screen) return@launch
            withContext(Dispatchers.Main.immediate) {
                if (pendingRailScreen != screen) return@withContext
                navigate(screen)
            }
        }
    }

    /** Cancel pending tab switch when focus leaves before settle completes. */
    fun onRailItemUnfocused(screen: Screens) {
        if (pendingRailScreen == screen) {
            pendingRailScreen = null
            railNavigateJob?.cancel()
        }
    }

    fun cancelPendingRailNavigation() {
        pendingRailScreen = null
        railNavigateJob?.cancel()
    }

    private fun prefetchRailScreen(screen: Screens) {
        when (screen) {
            Screens.Home -> {
                prefetchCatalog(screen)
                prefetchContinueWatching()
            }
            Screens.Favourites -> prefetchMyLibrary()
            Screens.Profile -> prefetchProfile()
            else -> prefetchCatalog(screen)
        }
    }

    private fun prefetchContinueWatching() {
        val userId = authSessionStore.currentUserId() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { libraryRepository.fetchContinueWatchingMovies(userId) }
        }
    }

    private fun prefetchMyLibrary() {
        val userId = authSessionStore.currentUserId() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { libraryRepository.prefetchMyLibrary(userId) }
        }
    }

    private fun prefetchProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { authRepository.refreshAuthState() }
        }
    }

    private fun prefetchCatalog(screen: Screens) {
        catalogPageKey(screen)?.let { page ->
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { movieRepository.prefetchHomePage(page) }
            }
        }
    }

    fun isHomeCatalogCached(): Boolean =
        !movieRepository.peekHomeSections(BrewPages.HOME).isNullOrEmpty()
}

fun catalogPageKey(screen: Screens): String? = when (screen) {
    Screens.Home -> BrewPages.HOME
    Screens.BrewPlus -> BrewPages.BREW_PLUS
    Screens.Shorts -> BrewPages.SHORTS
    Screens.Store -> BrewPages.STORE
    else -> null
}

fun isCatalogScreen(screen: Screens): Boolean = catalogPageKey(screen) != null
