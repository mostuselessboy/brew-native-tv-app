package com.google.jetstream.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 450L
private const val MIN_QUERY_LENGTH = 2

data class SearchUiState(
    val query: String = "",
    val debouncedQuery: String = "",
    val results: MovieList = emptyList(),
    val suggestions: MovieList = emptyList(),
    val isFetching: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val suggestions = MutableStateFlow<MovieList>(emptyList())
    private val results = MutableStateFlow<MovieList>(emptyList())
    private val isFetching = MutableStateFlow(false)
    private val debouncedQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            movieRepository.getFeaturedMovies().collect { featured ->
                suggestions.value = featured.take(42)
            }
        }
        viewModelScope.launch {
            query
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { text ->
                    val trimmed = text.trim()
                    debouncedQuery.value = trimmed
                    if (trimmed.length < MIN_QUERY_LENGTH) {
                        results.value = emptyList()
                        isFetching.value = false
                        return@collect
                    }
                    isFetching.value = true
                    results.value = runCatching {
                        movieRepository.searchMovies(trimmed)
                    }.getOrDefault(emptyList())
                    isFetching.value = false
                }
        }
    }

    fun setQuery(text: String) {
        query.value = text
    }

    val uiState = combine(
        query,
        debouncedQuery,
        results,
        suggestions,
        isFetching,
    ) { q, debounced, res, reco, fetching ->
        SearchUiState(
            query = q,
            debouncedQuery = debounced,
            results = res,
            suggestions = reco,
            isFetching = fetching || (q.trim().length >= MIN_QUERY_LENGTH && debounced != q.trim()),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(isFetching = true),
    )
}
