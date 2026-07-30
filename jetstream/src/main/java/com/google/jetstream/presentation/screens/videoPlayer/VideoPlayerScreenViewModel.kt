package com.google.jetstream.presentation.screens.videoPlayer

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class VideoPlayerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MovieRepository,
) : ViewModel() {
    val uiState = savedStateHandle
        .getStateFlow<String?>(VideoPlayerScreen.MovieIdBundleKey, null)
        .map { rawId ->
            val id = rawId?.let { Uri.decode(it) }
            if (id.isNullOrBlank()) {
                VideoPlayerScreenUiState.Error
            } else {
                runCatching {
                    repository.getMovieDetails(movieId = id)
                }.fold(
                    onSuccess = { VideoPlayerScreenUiState.Done(movieDetails = it) },
                    onFailure = { VideoPlayerScreenUiState.Error },
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VideoPlayerScreenUiState.Loading
        )
}

@Immutable
sealed class VideoPlayerScreenUiState {
    data object Loading : VideoPlayerScreenUiState()
    data object Error : VideoPlayerScreenUiState()
    data class Done(val movieDetails: MovieDetails) : VideoPlayerScreenUiState()
}
