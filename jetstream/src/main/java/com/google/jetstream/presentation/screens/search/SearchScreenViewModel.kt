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

package com.google.jetstream.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 450L

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val internalSearchState = MutableSharedFlow<SearchState>()

    private val _selectedFilter = MutableStateFlow(SearchFilter.All)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private var debounceJob: Job? = null

    init {
        viewModelScope.launch { loadSuggestions() }
    }

    /**
     * Called on every keystroke. Blank query immediately reloads suggestions.
     * Non-blank queries are debounced by 450ms to avoid firing on every keystroke.
     */
    fun query(queryString: String) {
        debounceJob?.cancel()
        _selectedFilter.value = SearchFilter.All
        if (queryString.isBlank()) {
            debounceJob = viewModelScope.launch { loadSuggestions() }
            return
        }
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            postQuery(queryString)
        }
    }

    /**
     * Filter changes are instant — no network call.
     * The UI derives the filtered list via remember(movieList, selectedFilter).
     */
    fun setFilter(filter: SearchFilter) {
        _selectedFilter.value = filter
    }

    private suspend fun loadSuggestions() {
        internalSearchState.emit(SearchState.Searching)
        runCatching {
            val dice = movieRepository.getDiceSuggestions()
            // Emit raw unfiltered list — UI applies the filter synchronously via remember()
            internalSearchState.emit(
                SearchState.Done(
                    movieList = dice.movies,
                    sectionTitle = dice.title,
                    sectionSubheading = dice.subheading,
                    isSuggestions = true,
                ),
            )
        }.onFailure {
            internalSearchState.emit(
                SearchState.Done(movieList = emptyList(), sectionTitle = "Discover", isSuggestions = true),
            )
        }
    }

    private suspend fun postQuery(queryString: String) {
        internalSearchState.emit(SearchState.Searching)
        runCatching {
            val result = movieRepository.searchMovies(query = queryString)
            // Emit raw unfiltered list — UI applies the filter synchronously via remember()
            internalSearchState.emit(
                SearchState.Done(
                    movieList = result,
                    sectionTitle = "Results",
                    searchQuery = queryString.trim(),
                    isSuggestions = false,
                ),
            )
        }.onFailure {
            internalSearchState.emit(
                SearchState.Done(
                    movieList = emptyList(),
                    sectionTitle = "Results",
                    searchQuery = queryString.trim(),
                    isSuggestions = false,
                ),
            )
        }
    }

    val searchState = internalSearchState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState.Loading,
    )
}

sealed interface SearchState {
    data object Loading : SearchState
    data object Searching : SearchState
    data class Done(
        val movieList: MovieList,
        val sectionTitle: String? = null,
        val sectionSubheading: String? = null,
        val searchQuery: String? = null,
        val isSuggestions: Boolean = false,
    ) : SearchState
}
