package com.google.jetstream.presentation.screens.movies

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MovieDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {

    private val movieId: String? = savedStateHandle.get<String>(MovieDetailsScreen.MovieIdBundleKey)
        ?.let { Uri.decode(it) }
        ?.takeIf { it.isNotBlank() }

    val uiState = flow {
        val id = movieId
        if (id == null) {
            emit(MovieDetailsScreenUiState.Error)
            return@flow
        }
        emit(MovieDetailsScreenUiState.Loading)
        repeat(3) { attempt ->
            if (attempt > 0) delay(350L * attempt)
            val result = runCatching { repository.getMovieDetails(movieId = id) }
            result.onSuccess {
                emit(MovieDetailsScreenUiState.Done(movieDetails = it))
                return@flow
            }
        }
        emit(MovieDetailsScreenUiState.Error)
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MovieDetailsScreenUiState.Loading,
        )
}

sealed class MovieDetailsScreenUiState {
    data object Loading : MovieDetailsScreenUiState()
    data object Error : MovieDetailsScreenUiState()
    data class Done(val movieDetails: MovieDetails) : MovieDetailsScreenUiState()
}
