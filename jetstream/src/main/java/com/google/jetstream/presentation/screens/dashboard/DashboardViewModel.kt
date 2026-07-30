package com.google.jetstream.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.presentation.screens.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _restoreFocusMovieId = MutableStateFlow<String?>(null)
    val restoreFocusMovieId: StateFlow<String?> = _restoreFocusMovieId.asStateFlow()

    fun rememberMovieFocus(movieId: String) {
        _restoreFocusMovieId.value = movieId
    }

    fun clearRestoreFocusMovieId() {
        _restoreFocusMovieId.value = null
    }

    init {
        // Only warm Home on launch — other catalog tabs prefetch on rail focus.
        viewModelScope.launch {
            runCatching { movieRepository.prefetchHomePage(BrewPages.HOME) }
        }
    }

    fun prefetchCatalog(screen: Screens) {
        catalogPageKey(screen)?.let { page ->
            viewModelScope.launch {
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
