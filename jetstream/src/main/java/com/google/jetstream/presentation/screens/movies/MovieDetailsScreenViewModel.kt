package com.google.jetstream.presentation.screens.movies

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.playback.PlaybackIntentStore
import com.google.jetstream.data.remote.BrewCheckPurchaseResponse
import com.google.jetstream.data.repositories.LibraryRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.PlaybackRepository
import com.google.jetstream.data.util.DetailCtaKind
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.data.util.EffectivePurchaseCta
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
class MovieDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val playbackIntentStore: PlaybackIntentStore,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _bookmarkState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Idle)
    val bookmarkState: StateFlow<BookmarkUiState> = _bookmarkState.asStateFlow()

    private val _playbackLoading = MutableStateFlow(false)
    val playbackLoading: StateFlow<Boolean> = _playbackLoading.asStateFlow()

    private val _navigateToPlayer = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToPlayer: SharedFlow<String> = _navigateToPlayer.asSharedFlow()

    val uiState = savedStateHandle
        .getStateFlow<String?>(MovieDetailsScreen.MovieIdBundleKey, null)
        .flatMapLatest { rawId ->
            flow {
                emit(MovieDetailsScreenUiState.Loading)
                val id = rawId?.let { Uri.decode(it) }?.trim()
                if (id.isNullOrBlank()) {
                    emit(MovieDetailsScreenUiState.Error)
                    return@flow
                }
                emit(
                    runCatching { repository.getMovieDetails(movieId = id) }.fold(
                        onSuccess = { details ->
                            val enriched = enrichWithPurchase(details)
                            loadBookmarkStatus(enriched.details, enriched.checkPurchase)
                            MovieDetailsScreenUiState.Done(
                                movieDetails = enriched.details,
                                checkPurchase = enriched.checkPurchase,
                                purchaseLoading = enriched.purchaseLoading,
                            )
                        },
                        onFailure = { MovieDetailsScreenUiState.Error },
                    ),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MovieDetailsScreenUiState.Loading,
        )

    fun onPrimaryCtaClick(details: MovieDetails, checkPurchase: BrewCheckPurchaseResponse?) {
        val slot = DetailPurchaseCta.primaryRowSlots(details).firstOrNull() ?: return
        onCtaClick(details, checkPurchase, slot)
    }

    fun onSecondaryCtaClick(details: MovieDetails, checkPurchase: BrewCheckPurchaseResponse?) {
        val slots = DetailPurchaseCta.primaryRowSlots(details)
        val slot = slots.getOrNull(1) ?: return
        onCtaClick(details, checkPurchase, slot)
    }

    fun onTrailerClick(details: MovieDetails) {
        viewModelScope.launch {
            if (_playbackLoading.value) return@launch
            _playbackLoading.value = true
            val intent = playbackRepository.prepareTrailerPlayback(details)
            _playbackLoading.value = false
            if (intent != null) {
                playbackIntentStore.set(intent)
                _navigateToPlayer.tryEmit(details.id)
            }
        }
    }

    fun toggleBookmark(movieDetails: MovieDetails) {
        val vodAssetId = movieDetails.vodAssetId ?: return
        val userId = authSessionStore.currentUserId() ?: return
        if (_bookmarkState.value is BookmarkUiState.Loading) return

        val currentlyBookmarked = (_bookmarkState.value as? BookmarkUiState.Ready)?.isBookmarked == true
        viewModelScope.launch {
            _bookmarkState.value = BookmarkUiState.Loading
            val result = if (currentlyBookmarked) {
                libraryRepository.removeBookmark(userId, vodAssetId)
            } else {
                libraryRepository.addBookmark(userId, vodAssetId)
            }
            _bookmarkState.value = result.fold(
                onSuccess = { BookmarkUiState.Ready(isBookmarked = !currentlyBookmarked) },
                onFailure = {
                    BookmarkUiState.Ready(isBookmarked = currentlyBookmarked)
                },
            )
        }
    }

    private fun onCtaClick(
        details: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
        slot: com.google.jetstream.data.util.DetailPurchaseCtaSlot,
    ) {
        if (playbackRepository.isWatchCta(slot.kind)) {
            startFeaturePlayback(details, checkPurchase)
            return
        }
        when (slot.kind) {
            DetailCtaKind.Rent,
            DetailCtaKind.Buy,
            DetailCtaKind.SubscribeYearly,
            DetailCtaKind.SubscribeQuarterly -> Unit
            else -> onTrailerClick(details)
        }
    }

    private fun startFeaturePlayback(
        details: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
    ) {
        val userId = authSessionStore.currentUserId()
        if (userId == null) {
            onTrailerClick(details)
            return
        }
        viewModelScope.launch {
            if (_playbackLoading.value) return@launch
            _playbackLoading.value = true
            val result = playbackRepository.prepareFeaturePlayback(
                movie = details,
                checkPurchase = checkPurchase,
                userId = userId,
            )
            _playbackLoading.value = false
            result.onSuccess { intent ->
                playbackIntentStore.set(intent)
                _navigateToPlayer.tryEmit(details.id)
            }.onFailure {
                playbackRepository.prepareTrailerPlayback(details)?.let { trailer ->
                    playbackIntentStore.set(trailer)
                    _navigateToPlayer.tryEmit(details.id)
                }
            }
        }
    }

    private suspend fun enrichWithPurchase(details: MovieDetails): EnrichedDetails {
        val userId = authSessionStore.currentUserId()
        if (userId == null || !authSessionStore.isAuthenticated()) {
            return EnrichedDetails(details = details, checkPurchase = null, purchaseLoading = false)
        }
        val purchase = playbackRepository.checkPurchase(
            userId = userId,
            cvName = details.cvName.ifBlank { details.id },
            campaignVersionId = details.campaignVersionId,
        )
        val mergedSlots = EffectivePurchaseCta.mergePurchaseCtaSlots(details, purchase)
        val mergedDetails = details.copy(purchaseCtaSlots = mergedSlots)
        if (purchase?.isBookmarked == true) {
            _bookmarkState.value = BookmarkUiState.Ready(isBookmarked = true)
        }
        return EnrichedDetails(
            details = mergedDetails,
            checkPurchase = purchase,
            purchaseLoading = false,
        )
    }

    private fun loadBookmarkStatus(
        details: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
    ) {
        if (checkPurchase?.isBookmarked == true) {
            _bookmarkState.value = BookmarkUiState.Ready(isBookmarked = true)
            return
        }
        val vodAssetId = details.vodAssetId
        val userId = authSessionStore.currentUserId()
        if (vodAssetId == null || userId == null || !authSessionStore.isAuthenticated()) {
            _bookmarkState.value = BookmarkUiState.Idle
            return
        }
        viewModelScope.launch {
            val bookmarked = libraryRepository.getBookmarkStatus(userId, vodAssetId)
            _bookmarkState.value = BookmarkUiState.Ready(isBookmarked = bookmarked)
        }
    }

    private data class EnrichedDetails(
        val details: MovieDetails,
        val checkPurchase: BrewCheckPurchaseResponse?,
        val purchaseLoading: Boolean,
    )
}

sealed class BookmarkUiState {
    data object Idle : BookmarkUiState()
    data object Loading : BookmarkUiState()
    data class Ready(val isBookmarked: Boolean) : BookmarkUiState()
}

sealed class MovieDetailsScreenUiState {
    data object Loading : MovieDetailsScreenUiState()
    data object Error : MovieDetailsScreenUiState()
    data class Done(
        val movieDetails: MovieDetails,
        val checkPurchase: BrewCheckPurchaseResponse? = null,
        val purchaseLoading: Boolean = false,
    ) : MovieDetailsScreenUiState()
}
