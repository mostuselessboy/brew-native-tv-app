package com.google.jetstream.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.presentation.screens.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

private val PrefetchCatalogPages = listOf(
    BrewPages.HOME,
    BrewPages.BREW_PLUS,
    BrewPages.SHORTS,
    BrewPages.STORE,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            PrefetchCatalogPages.forEach { page ->
                runCatching { movieRepository.prefetchHomePage(page) }
            }
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
