/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.LibraryRepository
import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.util.BrewImageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface ContinueWatchingTrayState {
    data object Hidden : ContinueWatchingTrayState
    data object Loading : ContinueWatchingTrayState
    data class Ready(val movies: List<Movie>) : ContinueWatchingTrayState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeScreeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val libraryRepository: LibraryRepository,
    private val authSessionStore: AuthSessionStore,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val page = MutableStateFlow<String?>(null)
    private val _continueWatchingState =
        MutableStateFlow<ContinueWatchingTrayState>(ContinueWatchingTrayState.Hidden)
    val continueWatchingState: StateFlow<ContinueWatchingTrayState> =
        _continueWatchingState.asStateFlow()

    fun peekInitialState(pageKey: String): HomeScreenUiState {
        val cached = movieRepository.peekHomeSections(pageKey)
        return if (cached.isNullOrEmpty()) HomeScreenUiState.Loading else HomeScreenUiState.Ready(cached)
    }

    fun setPage(pageKey: String) {
        page.value = pageKey
        if (pageKey == BrewPages.HOME) {
            refreshContinueWatching()
        }
    }

    private fun refreshContinueWatching() {
        val userId = authSessionStore.currentUserId()
        if (userId == null || userId <= 0) {
            _continueWatchingState.value = ContinueWatchingTrayState.Hidden
            return
        }
        _continueWatchingState.value = ContinueWatchingTrayState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                libraryRepository.fetchContinueWatchingMovies(userId)
            }.onSuccess { movies ->
                _continueWatchingState.value = if (movies.isEmpty()) {
                    ContinueWatchingTrayState.Hidden
                } else {
                    ContinueWatchingTrayState.Ready(movies)
                }
            }.onFailure {
                _continueWatchingState.value = ContinueWatchingTrayState.Hidden
            }
        }
    }

    val uiState: StateFlow<HomeScreenUiState> = page
        .filterNotNull()
        .flatMapLatest { pageKey ->
            flow {
                val cached = movieRepository.peekHomeSections(pageKey)
                if (cached.isNullOrEmpty()) {
                    emit(HomeScreenUiState.Loading)
                } else {
                    emit(HomeScreenUiState.Ready(cached))
                }
                movieRepository.getHomeSections(pageKey).collect { sections ->
                    emit(
                        if (sections.isEmpty()) HomeScreenUiState.Error
                        else HomeScreenUiState.Ready(sections)
                    )
                }
            }
        }
        .onEach { state ->
            if (state is HomeScreenUiState.Ready) {
                prefetchImages(state.sections)
            }
        }
        .catch { emit(HomeScreenUiState.Error) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeScreenUiState.Loading,
        )

    private fun prefetchImages(sections: List<HomeSection>) {
        viewModelScope.launch(Dispatchers.IO) {
            sections.firstOrNull { it.type == HomeSectionType.Showcase }?.movies
                ?.take(4)
                ?.forEach { movie ->
                    context.imageLoader.enqueue(
                        ImageRequest.Builder(context)
                            .data(BrewImageUrl.forShowcase(movie.posterUri))
                            .size(BrewImageUrl.SHOWCASE_WIDTH, BrewImageUrl.SHOWCASE_HEIGHT)
                            .build()
                    )
                }
            delay(2500)
            sections
                .asSequence()
                .filter {
                    it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive
                }
                .take(4)
                .flatMap { it.movies.asSequence().take(8) }
                .forEach { movie ->
                    context.imageLoader.enqueue(
                        ImageRequest.Builder(context)
                            .data(BrewImageUrl.forCard(movie.posterUri))
                            .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
                            .build()
                    )
                }
        }
    }
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data object Error : HomeScreenUiState
    data class Ready(val sections: List<HomeSection>) : HomeScreenUiState
}
