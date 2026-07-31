package com.google.jetstream.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthRepository
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

private val PrefetchCatalogPages = listOf(
    BrewPages.HOME,
    BrewPages.BREW_PLUS,
    BrewPages.SHORTS,
    BrewPages.STORE,
)

private const val RailSettleDebounceMs = 240L
private const val RailSettleDebounceCachedMs = 140L

enum class CatalogFocusRestoreTarget {
    ShowcasePrimary,
    FirstTray,
}

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

    var focusRestoreTarget: CatalogFocusRestoreTarget = CatalogFocusRestoreTarget.ShowcasePrimary
        private set

    fun rememberFocusTarget(target: CatalogFocusRestoreTarget) {
        focusRestoreTarget = target
    }

    private var railNavigateJob: Job? = null
    private var pendingRailScreen: Screens? = null

    fun playMovie(movie: Movie) {
        viewModelScope.launch {
            playbackLauncher.launchMovie(movie)
                .onSuccess { _openPlayer.tryEmit(it) }
        }
    }

    fun playLibraryItem(item: LibraryItem, openDetail: (String) -> Unit) {
        viewModelScope.launch {
            playbackLauncher.launchLibraryItem(item)
                .onSuccess { _openPlayer.tryEmit(it) }
                .onFailure { error ->
                    if (error.message == "Open detail") {
                        openDetail(item.movieId)
                    }
                }
        }
    }

    /** Prefetch on IO; navigate only after focus dwells (release/settle). */
    fun onRailItemFocused(
        screen: Screens,
        selected: Boolean,
        navigate: (Screens) -> Unit,
    ) {
        prefetchRailScreen(screen)
        if (selected) return

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
}

fun catalogPageKey(screen: Screens): String? = when (screen) {
    Screens.Home -> BrewPages.HOME
    Screens.BrewPlus -> BrewPages.BREW_PLUS
    Screens.Shorts -> BrewPages.SHORTS
    Screens.Store -> BrewPages.STORE
    else -> null
}

fun isCatalogScreen(screen: Screens): Boolean = catalogPageKey(screen) != null
