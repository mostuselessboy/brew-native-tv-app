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
import com.google.jetstream.data.playback.PlaybackProgressNotifier
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

data class AccessDialogState(
    val title: String,
    val message: String,
    val showSignInButton: Boolean,
    val showBuyButton: Boolean
)

@HiltViewModel
class MovieDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val playbackIntentStore: PlaybackIntentStore,
    private val authSessionStore: AuthSessionStore,
    private val playbackProgressNotifier: PlaybackProgressNotifier,
) : ViewModel() {

    init {
        viewModelScope.launch {
            playbackProgressNotifier.events.collect {
                refreshAfterPlayback()
            }
        }
    }

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

    private val _selectedCastMemberDetails = MutableStateFlow<com.google.jetstream.data.remote.BrewCastMemberDetailDto?>(null)
    val selectedCastMemberDetails: StateFlow<com.google.jetstream.data.remote.BrewCastMemberDetailDto?> = _selectedCastMemberDetails.asStateFlow()

    private val _castLoading = MutableStateFlow(false)
    val castLoading: StateFlow<Boolean> = _castLoading.asStateFlow()

    private val _accessDialogState = MutableStateFlow<AccessDialogState?>(null)
    val accessDialogState: StateFlow<AccessDialogState?> = _accessDialogState.asStateFlow()

    private val _optimisticReminderSet = MutableStateFlow(false)
    private val _reminderFeedback = MutableStateFlow<com.google.jetstream.presentation.common.BrewFeedbackMessage?>(null)
    val reminderFeedback: StateFlow<com.google.jetstream.presentation.common.BrewFeedbackMessage?> =
        _reminderFeedback.asStateFlow()

    fun dismissAccessDialog() {
        _accessDialogState.value = null
    }

    fun loadCastMemberDetails(castMemberId: String) {
        viewModelScope.launch {
            _castLoading.value = true
            _selectedCastMemberDetails.value = repository.getCastMember(castMemberId)
            _castLoading.value = false
        }
    }

    fun dismissCastDialog() {
        _selectedCastMemberDetails.value = null
    }

    private val movieIdFlow = savedStateHandle
        .getStateFlow<String?>(MovieDetailsScreen.MovieIdBundleKey, null)

    init {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(movieIdFlow, authSessionStore.currentUser) { rawId, _ ->
                rawId
            }.collect { rawId ->
                loadMovie(rawId)
            }
        }
    }

    private suspend fun loadMovie(rawId: String?) {
        val id = rawId?.let { Uri.decode(it) }?.trim()
        val peekPoster = id?.let { repository.peekMovieFromCatalog(it)?.posterUri }
        _optimisticReminderSet.value = false
        _uiState.value = MovieDetailsScreenUiState.Loading(posterUri = peekPoster)
        if (id.isNullOrBlank()) {
            _uiState.value = MovieDetailsScreenUiState.Error
            return
        }
        runCatching { repository.getMovieDetails(movieId = id) }.fold(
            onSuccess = { details ->
                _uiState.value = MovieDetailsScreenUiState.Done(
                    movieDetails = details,
                    checkPurchase = null,
                    purchaseLoading = true,
                )
                viewModelScope.launch {
                    runCatching { enrichWithPurchase(details) }.fold(
                        onSuccess = { enriched ->
                            loadBookmarkStatus(enriched.details, enriched.checkPurchase)
                            _uiState.value = MovieDetailsScreenUiState.Done(
                                movieDetails = enriched.details,
                                checkPurchase = enriched.checkPurchase,
                                purchaseLoading = false,
                            )
                        },
                        onFailure = {
                            _uiState.value = MovieDetailsScreenUiState.Done(
                                movieDetails = details,
                                checkPurchase = null,
                                purchaseLoading = false,
                            )
                        }
                    )
                }
            },
            onFailure = {
                _uiState.value = MovieDetailsScreenUiState.Error
            },
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
        _navigateToPlayer.tryEmit(details.id)
        viewModelScope.launch {
            val userId = authSessionStore.currentUserId() ?: 0
            playbackRepository.prepareTrailerPlayback(details, userId)?.let { intent ->
                playbackIntentStore.set(intent)
            }
        }
    }

    fun onExtraClick(details: MovieDetails, vodAssetId: Int, title: String) {
        val userId = authSessionStore.currentUserId()
        if (userId == null) {
            _accessDialogState.value = AccessDialogState(
                title = "Sign In Required",
                message = "Please sign in to watch this bonus content.",
                showSignInButton = true,
                showBuyButton = false
            )
            return
        }
        if (vodAssetId <= 0) return
        _navigateToPlayer.tryEmit(details.id)
        viewModelScope.launch {
            val result = playbackRepository.prepareDirectPlayback(
                userId = userId,
                cvName = details.cvName.ifBlank { details.id },
                vodAssetId = vodAssetId,
                title = title,
                campaignVersionId = details.campaignVersionId,
            )
            result.onSuccess { intent ->
                playbackIntentStore.set(intent)
            }.onFailure {
                _accessDialogState.value = AccessDialogState(
                    title = "Purchase Required",
                    message = "You need to purchase this title or subscribe to Brew+ to watch this content.",
                    showSignInButton = false,
                    showBuyButton = true
                )
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

    private fun refreshAfterPlayback() {
        viewModelScope.launch { refreshPurchaseOnly() }
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
            DetailCtaKind.SupportFilmmaker -> BrewWebUrls.moviePage(details)
            else -> BrewWebUrls.moviePage(details)
        }
        val title = when (slot.kind) {
            DetailCtaKind.Rent -> "Rent on brew.tv"
            DetailCtaKind.Buy -> "Buy on brew.tv"
            DetailCtaKind.SubscribeYearly,
            DetailCtaKind.SubscribeQuarterly -> "Subscribe on brew.tv"
            DetailCtaKind.SupportFilmmaker -> "Support on brew.tv"
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
            DetailCtaKind.SupportFilmmaker ->
                "Scan to support the filmmaker on brew.tv with your phone or tablet."
            else -> "Scan to continue on brew.tv."
        }
        _qrPopup.value = BrewQrPopupState(
            qrUrl = url,
            title = title,
            message = message,
            posterUri = details.posterUri,
            icon = BrewQrPopupIcon.Brew,
            doneAction = BrewQrPopupDoneAction.RefreshPurchase,
        )
    }

    private fun opensPurchaseQrPopup(kind: DetailCtaKind): Boolean = when (kind) {
        DetailCtaKind.Rent,
        DetailCtaKind.Buy,
        DetailCtaKind.SubscribeYearly,
        DetailCtaKind.SubscribeQuarterly,
        DetailCtaKind.SupportFilmmaker -> true
        else -> false
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
        if (opensPurchaseQrPopup(slot.kind)) {
            showPurchaseQrPopup(details, slot)
            return
        }
        when (slot.kind) {
            DetailCtaKind.ComingSoon,
            DetailCtaKind.ComingSoonNotify -> toggleReminder(details)
            else -> onTrailerClick(details)
        }
    }

    fun dismissReminderFeedback() {
        _reminderFeedback.value = null
    }

    fun isReminderSet(details: MovieDetails): Boolean {
        if (_optimisticReminderSet.value) return true
        return details.purchaseCtaSlots.any { slot ->
            (slot.kind == DetailCtaKind.ComingSoonNotify || slot.kind == DetailCtaKind.ComingSoon) &&
                slot.sublabel == "Reminder set"
        }
    }

    fun toggleReminder(details: MovieDetails) {
        if (isReminderSet(details)) {
            _reminderFeedback.value = com.google.jetstream.presentation.common.BrewFeedbackMessage(
                title = "You're on the list",
                message = "We'll let you know when this title is live.",
            )
            return
        }
        val userId = authSessionStore.currentUserId()
        if (userId == null) {
            _accessDialogState.value = AccessDialogState(
                title = "Sign In Required",
                message = "Please sign in to set a reminder for this title.",
                showSignInButton = true,
                showBuyButton = false
            )
            return
        }
        val campaignVersionId = details.campaignVersionId ?: return
        _optimisticReminderSet.value = true
        viewModelScope.launch {
            val result = repository.joinWaitlist(userId, campaignVersionId)
            result.onSuccess {
                refreshPurchaseOnly()
                _reminderFeedback.value = com.google.jetstream.presentation.common.BrewFeedbackMessage(
                    title = "You're on the list",
                    message = "We'll let you know when this title is live.",
                )
            }.onFailure {
                _optimisticReminderSet.value = false
                _reminderFeedback.value = com.google.jetstream.presentation.common.BrewFeedbackMessage(
                    title = "Could not save your reminder",
                    message = "Please try again.",
                )
            }
        }
    }

    private fun startFeaturePlayback(
        details: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
    ) {
        val userId = authSessionStore.currentUserId()
        if (userId == null) {
            _accessDialogState.value = AccessDialogState(
                title = "Sign In Required",
                message = "Please sign in to watch this title.",
                showSignInButton = true,
                showBuyButton = false
            )
            return
        }
        _navigateToPlayer.tryEmit(details.id)
        viewModelScope.launch {
            val result = playbackRepository.prepareFeaturePlayback(
                movie = details,
                checkPurchase = checkPurchase,
                userId = userId,
            )
            result.onSuccess { intent ->
                playbackIntentStore.set(intent)
            }.onFailure {
                val fallbackUserId = authSessionStore.currentUserId() ?: 0
                playbackRepository.prepareTrailerPlayback(details, fallbackUserId)?.let { trailer ->
                    playbackIntentStore.set(trailer)
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
