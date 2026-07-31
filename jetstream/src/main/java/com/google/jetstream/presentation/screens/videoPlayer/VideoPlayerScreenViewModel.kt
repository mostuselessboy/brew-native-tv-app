package com.google.jetstream.presentation.screens.videoPlayer

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.entities.EndScreenRecommendation
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.data.playback.PlaybackIntentStore
import com.google.jetstream.data.playback.PlaybackLauncher
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VideoPlayerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
    private val playbackRepository: PlaybackRepository,
    private val playbackIntentStore: PlaybackIntentStore,
    private val playbackLauncher: PlaybackLauncher,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val reloadToken = MutableStateFlow(0)

    private val _endScreenState = MutableStateFlow<EndScreenUiState>(EndScreenUiState.Hidden)
    val endScreenState: StateFlow<EndScreenUiState> = _endScreenState.asStateFlow()

    private val _switchToMovie = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val switchToMovie: SharedFlow<String> = _switchToMovie.asSharedFlow()

    val uiState = savedStateHandle
        .getStateFlow<String?>(VideoPlayerScreen.MovieIdBundleKey, null)
        .flatMapLatest { rawId ->
            flow {
                reloadToken.value
                emit(VideoPlayerScreenUiState.Loading)
                val id = rawId?.let { Uri.decode(it) }?.trim()
                if (id.isNullOrBlank()) {
                    emit(VideoPlayerScreenUiState.Error)
                    return@flow
                }
                val details = runCatching {
                    repository.getMovieDetails(movieId = id)
                }.getOrElse {
                    emit(VideoPlayerScreenUiState.Error)
                    return@flow
                }
                val intent = playbackIntentStore.consume()?.takeIf { it.movieSlug == id }
                    ?: resolvePlaybackIntent(details)
                if (intent == null || intent.hlsUrl.isBlank()) {
                    emit(VideoPlayerScreenUiState.Error)
                    return@flow
                }
                emit(
                    VideoPlayerScreenUiState.Done(
                        movieDetails = details,
                        playback = intent,
                    ),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VideoPlayerScreenUiState.Loading,
        )

    fun onPlaybackEnded(details: MovieDetails) {
        val campaignId = details.campaignId ?: return
        val projectType = details.projectType?.trim().orEmpty()
        if (projectType.isBlank()) return
        if (_endScreenState.value is EndScreenUiState.Loading ||
            _endScreenState.value is EndScreenUiState.Ready
        ) {
            return
        }

        viewModelScope.launch {
            _endScreenState.value = EndScreenUiState.Loading
            val picks = playbackRepository.fetchEndscreenRecommendations(
                campaignId = campaignId,
                projectType = projectType,
                userId = authSessionStore.currentUserId(),
                country = details.userCountry.ifBlank { "in" },
            )
            _endScreenState.value = if (picks.isEmpty()) {
                EndScreenUiState.Hidden
            } else {
                EndScreenUiState.Ready(picks)
            }
        }
    }

    fun dismissEndScreen() {
        _endScreenState.value = EndScreenUiState.Hidden
    }

    fun playEndScreenRecommendation(pick: EndScreenRecommendation) {
        viewModelScope.launch {
            _endScreenState.value = EndScreenUiState.Hidden
            playbackLauncher.launchEndScreenPick(pick)
                .onSuccess { slug -> _switchToMovie.tryEmit(slug) }
        }
    }

    fun reload() {
        reloadToken.value += 1
    }

    private suspend fun resolvePlaybackIntent(details: MovieDetails): PlaybackIntent? {
        val userId = authSessionStore.currentUserId()
        if (userId != null) {
            val checkPurchase = playbackRepository.checkPurchase(
                userId = userId,
                cvName = details.cvName.ifBlank { details.id },
                campaignVersionId = details.campaignVersionId,
            )
            playbackRepository.prepareFeaturePlayback(
                movie = details,
                checkPurchase = checkPurchase,
                userId = userId,
            ).getOrNull()?.let { return it }
        }
        return fallbackTrailerIntent(details)
    }

    private fun fallbackTrailerIntent(details: MovieDetails): PlaybackIntent? {
        val url = details.videoUri.takeIf { it.isNotBlank() } ?: return null
        return PlaybackIntent(
            movieSlug = details.id,
            title = details.name,
            hlsUrl = url,
            isTrailer = true,
        )
    }
}

@Immutable
sealed class VideoPlayerScreenUiState {
    data object Loading : VideoPlayerScreenUiState()
    data object Error : VideoPlayerScreenUiState()
    data class Done(
        val movieDetails: MovieDetails,
        val playback: PlaybackIntent?,
    ) : VideoPlayerScreenUiState()
}

@Immutable
sealed interface EndScreenUiState {
    data object Hidden : EndScreenUiState
    data object Loading : EndScreenUiState
    data class Ready(val picks: List<EndScreenRecommendation>) : EndScreenUiState
}
