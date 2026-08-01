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
import com.google.jetstream.data.util.BrewWebUrls
import com.google.jetstream.data.util.DetailCtaKind
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.data.util.DetailPurchaseCtaSlot
import com.google.jetstream.data.util.EffectivePurchaseCta
import com.google.jetstream.data.util.SubscriptionPlanMerge
import com.google.jetstream.presentation.common.BrewQrPopupDoneAction
import com.google.jetstream.presentation.common.BrewQrPopupIcon
import com.google.jetstream.presentation.common.BrewQrPopupState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _qrPopup = MutableStateFlow<BrewQrPopupState?>(null)
    val qrPopup: StateFlow<BrewQrPopupState?> = _qrPopup.asStateFlow()

    private val _uiState = MutableStateFlow<MovieDetailsScreenUiState>(
        MovieDetailsScreenUiState.Loading(),
    )
    val uiState: StateFlow<MovieDetailsScreenUiState> = _uiState.asStateFlow()

    private val movieIdFlow = savedStateHandle
        .getStateFlow<String?>(MovieDetailsScreen.MovieIdBundleKey, null)

    init {
        viewModelScope.launch {
            movieIdFlow.collect { rawId -> loadMovie(rawId) }
        }
    }

    private suspend fun loadMovie(rawId: String?) {
        val id = rawId?.let { Uri.decode(it) }?.trim()
        val peekPoster = id?.let { repository.peekMovieFromCatalog(it)?.posterUri }
        _uiState.value = MovieDetailsScreenUiState.Loading(posterUri = peekPoster)
        if (id.isNullOrBlank()) {
            _uiState.value = MovieDetailsScreenUiState.Error
            return
        }
        _uiState.value = runCatching { repository.getMovieDetails(movieId = id) }.fold(
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
        )
    }

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
        val youtubeUrl = details.trailerOriginalUrl?.takeIf { details.trailerIsYoutube }
        if (youtubeUrl != null) {
            showYoutubeTrailerPopup(youtubeUrl)
            return
        }
        viewModelScope.launch {
            if (_playbackLoading.value) return@launch
            _playbackLoading.value = true
            val userId = authSessionStore.currentUserId() ?: 0
            val intent = playbackRepository.prepareTrailerPlayback(details, userId)
            _playbackLoading.value = false
            if (intent != null) {
                playbackIntentStore.set(intent)
                _navigateToPlayer.tryEmit(details.id)
            }
        }
    }

    fun onExtraClick(details: MovieDetails, vodAssetId: Int, title: String) {
        val userId = authSessionStore.currentUserId() ?: return
        if (vodAssetId <= 0) return
        viewModelScope.launch {
            if (_playbackLoading.value) return@launch
            _playbackLoading.value = true
            val result = playbackRepository.prepareDirectPlayback(
                userId = userId,
                cvName = details.cvName.ifBlank { details.id },
                vodAssetId = vodAssetId,
                title = title,
                campaignVersionId = details.campaignVersionId,
            )
            _playbackLoading.value = false
            result.onSuccess { intent ->
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

    fun onShareClick(details: MovieDetails) {
        val url = BrewWebUrls.share(details)
        _qrPopup.value = BrewQrPopupState(
            qrUrl = url,
            title = "Share this title",
            message = "Scan with your phone to open this movie on brew.tv.",
            posterUri = details.posterUri,
            icon = BrewQrPopupIcon.Brew,
        )
    }

    fun onCriticReviewClick(link: String) {
        if (link.isBlank()) return
        _qrPopup.value = BrewQrPopupState(
            qrUrl = link,
            title = "Read the review",
            message = "Scan to open the critic article on your phone or tablet.",
            icon = BrewQrPopupIcon.Brew,
        )
    }

    fun dismissQrPopup() {
        _qrPopup.value = null
    }

    fun onQrPopupDone() {
        val shouldRefresh = _qrPopup.value?.doneAction == BrewQrPopupDoneAction.RefreshPurchase
        _qrPopup.value = null
        if (shouldRefresh) {
            viewModelScope.launch { refreshPurchaseOnly() }
        }
    }

    private suspend fun refreshPurchaseOnly() {
        val current = _uiState.value as? MovieDetailsScreenUiState.Done ?: return
        if (!authSessionStore.isAuthenticated() || authSessionStore.currentUserId() == null) return
        _uiState.value = current.copy(purchaseLoading = true)
        val enriched = enrichWithPurchase(current.movieDetails)
        loadBookmarkStatus(enriched.details, enriched.checkPurchase)
        _uiState.value = MovieDetailsScreenUiState.Done(
            movieDetails = enriched.details,
            checkPurchase = enriched.checkPurchase,
            purchaseLoading = false,
        )
    }

    private fun showYoutubeTrailerPopup(url: String) {
        _qrPopup.value = BrewQrPopupState(
            qrUrl = url,
            title = "Watch trailer",
            message = "Scan to watch the trailer on YouTube on your phone or tablet.",
            icon = BrewQrPopupIcon.Youtube,
        )
    }

    private fun showPurchaseQrPopup(details: MovieDetails, slot: DetailPurchaseCtaSlot) {
        val url = when (slot.kind) {
            DetailCtaKind.Rent -> BrewWebUrls.rent(details)
            DetailCtaKind.Buy -> BrewWebUrls.buy(details)
            DetailCtaKind.SubscribeYearly -> BrewWebUrls.subscribeYearly()
            DetailCtaKind.SubscribeQuarterly -> BrewWebUrls.subscribeQuarterly()
            else -> BrewWebUrls.moviePage(details)
        }
        val title = when (slot.kind) {
            DetailCtaKind.Rent -> "Rent on brew.tv"
            DetailCtaKind.Buy -> "Buy on brew.tv"
            DetailCtaKind.SubscribeYearly,
            DetailCtaKind.SubscribeQuarterly -> "Subscribe on brew.tv"
            else -> "Continue on brew.tv"
        }
        val message = when (slot.kind) {
            DetailCtaKind.Rent ->
                "Scan to complete your rental on brew.tv with your phone or tablet."
            DetailCtaKind.Buy ->
                "Scan to complete your purchase on brew.tv with your phone or tablet."
            DetailCtaKind.SubscribeYearly,
            DetailCtaKind.SubscribeQuarterly ->
                "Scan to subscribe to Brew+ on brew.tv with your phone or tablet."
            else -> "Scan to continue on brew.tv."
        }
        _qrPopup.value = BrewQrPopupState(
            qrUrl = url,
            title = title,
            message = message,
            icon = BrewQrPopupIcon.Brew,
            doneAction = BrewQrPopupDoneAction.RefreshPurchase,
        )
    }

    private fun onCtaClick(
        details: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
        slot: DetailPurchaseCtaSlot,
    ) {
        if (slot.kind == DetailCtaKind.NotAvailable) return
        if (playbackRepository.isWatchCta(slot.kind)) {
            startFeaturePlayback(details, checkPurchase)
            return
        }
        when (slot.kind) {
            DetailCtaKind.Rent,
            DetailCtaKind.Buy,
            DetailCtaKind.SubscribeYearly,
            DetailCtaKind.SubscribeQuarterly -> showPurchaseQrPopup(details, slot)
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
                val userId = authSessionStore.currentUserId() ?: 0
                playbackRepository.prepareTrailerPlayback(details, userId)?.let { trailer ->
                    playbackIntentStore.set(trailer)
                    _navigateToPlayer.tryEmit(details.id)
                }
            }
        }
    }

    private suspend fun enrichWithPurchase(details: MovieDetails): EnrichedDetails {
        val country = details.userCountry.ifBlank { "in" }
        val catalogPlans = playbackRepository.fetchSubscriptionPlans(country)
        val mergedSubscriptionPlans = SubscriptionPlanMerge.merge(
            catalogPlans,
            details.subscriptionPlans,
        )
        val detailsWithPlans = details.copy(subscriptionPlans = mergedSubscriptionPlans)

        val userId = authSessionStore.currentUserId()
        if (userId == null || !authSessionStore.isAuthenticated()) {
            val slots = EffectivePurchaseCta.mergePurchaseCtaSlots(detailsWithPlans, null)
            return EnrichedDetails(
                details = detailsWithPlans.copy(purchaseCtaSlots = slots),
                checkPurchase = null,
                purchaseLoading = false,
            )
        }
        val purchase = playbackRepository.checkPurchase(
            userId = userId,
            cvName = detailsWithPlans.cvName.ifBlank { detailsWithPlans.id },
            campaignVersionId = detailsWithPlans.campaignVersionId,
        )
        val mergedSlots = EffectivePurchaseCta.mergePurchaseCtaSlots(detailsWithPlans, purchase)
        val mergedDetails = detailsWithPlans.copy(purchaseCtaSlots = mergedSlots)
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
    data class Loading(val posterUri: String? = null) : MovieDetailsScreenUiState()
    data object Error : MovieDetailsScreenUiState()
    data class Done(
        val movieDetails: MovieDetails,
        val checkPurchase: BrewCheckPurchaseResponse? = null,
        val purchaseLoading: Boolean = false,
    ) : MovieDetailsScreenUiState()
}
