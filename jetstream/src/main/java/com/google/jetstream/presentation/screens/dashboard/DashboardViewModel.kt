package com.google.jetstream.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.presentation.screens.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PrefetchCatalogPages = listOf(
    BrewPages.HOME,
    BrewPages.BREW_PLUS,
    BrewPages.SHORTS,
    BrewPages.STORE,
)

private const val RailPreviewDebounceColdMs = 90L

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private var railPreviewJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coroutineScope {
                PrefetchCatalogPages.map { page ->
                    async { runCatching { movieRepository.prefetchHomePage(page) } }
                }.awaitAll()
            }
        }
    }

    /**
     * Prefetch on IO; navigate on main only after cache check.
     * Cached tabs switch instantly — fast rail skips intermediate targets.
     */
    fun onRailItemFocused(
        screen: Screens,
        selected: Boolean,
        navigate: (Screens) -> Unit,
    ) {
        if (selected) return

        railPreviewJob?.cancel()
        railPreviewJob = viewModelScope.launch(Dispatchers.Default) {
            val page = catalogPageKey(screen) ?: return@launch

            launch(Dispatchers.IO) {
                runCatching { movieRepository.prefetchHomePage(page) }
            }

            val cached = movieRepository.peekHomeSections(page)
            if (cached.isNullOrEmpty()) {
                delay(RailPreviewDebounceColdMs)
            }

            withContext(Dispatchers.Main.immediate) {
                navigate(screen)
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
