package com.google.jetstream.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.common.BrewFeedbackToast
import com.google.jetstream.presentation.common.BrewQrPopup
import com.google.jetstream.presentation.common.DetailsShimmerSkeleton
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused

object MovieDetailsScreen {
    const val MovieIdBundleKey = "movieId"
}

@Composable
fun MovieDetailsScreen(
    goToMoviePlayer: (movieId: String) -> Unit,
    goToSignIn: () -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    movieDetailsScreenViewModel: MovieDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by movieDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    val bookmarkState by movieDetailsScreenViewModel.bookmarkState.collectAsStateWithLifecycle()
    val qrPopup by movieDetailsScreenViewModel.qrPopup.collectAsStateWithLifecycle()
    val selectedCastMemberDetails by movieDetailsScreenViewModel.selectedCastMemberDetails.collectAsStateWithLifecycle()
    val castLoading by movieDetailsScreenViewModel.castLoading.collectAsStateWithLifecycle()
    val accessDialogState by movieDetailsScreenViewModel.accessDialogState.collectAsStateWithLifecycle()
    val reminderFeedback by movieDetailsScreenViewModel.reminderFeedback.collectAsStateWithLifecycle()

    val primaryCtaFocusRequester = remember { FocusRequester() }

    var selectedCastMember by remember { mutableStateOf<MovieCast?>(null) }

    LaunchedEffect(movieDetailsScreenViewModel) {
        movieDetailsScreenViewModel.navigateToPlayer.collect { movieId ->
            goToMoviePlayer(movieId)
        }
    }

    when (val s = uiState) {
        is MovieDetailsScreenUiState.Loading -> {
            BackHandler(onBack = onBackPressed)
            DetailsShimmerSkeleton(
                posterUri = s.posterUri,
                modifier = Modifier.fillMaxSize(),
            )
        }

        is MovieDetailsScreenUiState.Error -> {
            BackHandler(onBack = onBackPressed)
            Error(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Done -> {
            val isBookmarked = (bookmarkState as? BookmarkUiState.Ready)?.isBookmarked == true
            val reminderSet = movieDetailsScreenViewModel.isReminderSet(s.movieDetails)
            val onPrimaryCtaClick = remember(movieDetailsScreenViewModel, s.movieDetails, s.checkPurchase) {
                { movieDetailsScreenViewModel.onPrimaryCtaClick(s.movieDetails, s.checkPurchase) }
            }
            val onSecondaryCtaClick = remember(movieDetailsScreenViewModel, s.movieDetails, s.checkPurchase) {
                { movieDetailsScreenViewModel.onSecondaryCtaClick(s.movieDetails, s.checkPurchase) }
            }
            val onTrailerClick = remember(movieDetailsScreenViewModel, s.movieDetails) {
                { movieDetailsScreenViewModel.onTrailerClick(s.movieDetails) }
            }
            val onBookmarkClick = remember(movieDetailsScreenViewModel, s.movieDetails) {
                { movieDetailsScreenViewModel.toggleBookmark(s.movieDetails) }
            }
            val onExtraClick = remember(movieDetailsScreenViewModel, s.movieDetails) {
                { vodAssetId: Int, title: String ->
                    movieDetailsScreenViewModel.onExtraClick(s.movieDetails, vodAssetId, title)
                }
            }
            val onShareClick = remember(movieDetailsScreenViewModel, s.movieDetails) {
                { movieDetailsScreenViewModel.onShareClick(s.movieDetails) }
            }
            val onCriticReviewClick = remember(movieDetailsScreenViewModel) {
                movieDetailsScreenViewModel::onCriticReviewClick
            }
            val onCastMemberClick = remember(movieDetailsScreenViewModel) {
                { castMember: MovieCast ->
                    selectedCastMember = castMember
                    movieDetailsScreenViewModel.loadCastMemberDetails(castMember.id)
                }
            }

            BackHandler(enabled = qrPopup != null || selectedCastMember != null) {
                if (qrPopup != null) {
                    movieDetailsScreenViewModel.dismissQrPopup()
                } else {
                    selectedCastMember = null
                    movieDetailsScreenViewModel.dismissCastDialog()
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
            Details(
                movieDetails = s.movieDetails,
                onPrimaryCtaClick = onPrimaryCtaClick,
                onSecondaryCtaClick = onSecondaryCtaClick,
                onTrailerClick = onTrailerClick,
                onShareClick = onShareClick,
                onCriticReviewClick = onCriticReviewClick,
                onCastMemberClick = onCastMemberClick,
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                isBookmarked = isBookmarked,
                onBookmarkClick = onBookmarkClick,
                onExtraClick = onExtraClick,
                purchaseLoading = s.purchaseLoading,
                reminderSet = reminderSet,
                primaryFocusRequester = primaryCtaFocusRequester,
                modifier = Modifier.fillMaxSize(),
            )
            BrewFeedbackToast(
                message = reminderFeedback,
                onDismiss = movieDetailsScreenViewModel::dismissReminderFeedback,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
            )
            BrewQrPopup(
                state = qrPopup,
                onDismissRequest = movieDetailsScreenViewModel::dismissQrPopup,
                onDone = movieDetailsScreenViewModel::onQrPopupDone,
            )
            selectedCastMember?.let { castMember ->
                MovieDetailCastDialog(
                    castMember = castMember,
                    castDetails = selectedCastMemberDetails,
                    isLoading = castLoading,
                    currentMovieName = s.movieDetails.name,
                    currentMovieReleaseDateOrYear = s.movieDetails.releaseYear.takeIf { it.isNotBlank() } ?: s.movieDetails.releaseDate,
                    onDismissRequest = {
                        selectedCastMember = null
                        movieDetailsScreenViewModel.dismissCastDialog()
                    }
                )
            }
            accessDialogState?.let { dialogState ->
                MovieDetailAccessDialog(
                    showDialog = true,
                    title = dialogState.title,
                    message = dialogState.message,
                    showSignInButton = dialogState.showSignInButton,
                    showBuyButton = dialogState.showBuyButton,
                    onSignInClick = goToSignIn,
                    onBuyClick = {
                        runCatching { primaryCtaFocusRequester.requestFocus() }
                    },
                    onDismissRequest = movieDetailsScreenViewModel::dismissAccessDialog
                )
            }
            }
        }
    }
}

@Composable
private fun Details(
    movieDetails: MovieDetails,
    onPrimaryCtaClick: () -> Unit,
    onSecondaryCtaClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onCriticReviewClick: (String) -> Unit = {},
    onCastMemberClick: (MovieCast) -> Unit = {},
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    onExtraClick: (vodAssetId: Int, title: String) -> Unit = { _, _ -> },
    purchaseLoading: Boolean = false,
    reminderSet: Boolean = false,
    primaryFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showSynopsisDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val primaryCtaFocusRequester = primaryFocusRequester ?: remember { FocusRequester() }

    LaunchedEffect(movieDetails.id) {
        listState.scrollToItem(0, scrollOffset = 0)
        kotlinx.coroutines.delay(150)
        runCatching { primaryCtaFocusRequester.requestFocus() }
        listState.scrollToItem(0, scrollOffset = 0)
    }

    val customersAlsoWatched = remember(movieDetails) {
        filterDetailMovies(movieDetails.customersAlsoWatched, movieDetails.id)
    }
    val context = LocalContext.current
    LaunchedEffect(movieDetails.id) {
        val loader = context.imageLoader
        val avatarRequests = movieDetails.castAndCrew.take(8).map { member ->
            ImageRequest.Builder(context)
                .data(BrewImageUrl.forCastAvatar(member.avatarUrl))
                .size(BrewImageUrl.CAST_AVATAR_PX, BrewImageUrl.CAST_AVATAR_PX)
                .build()
        }
        val cardRequests = customersAlsoWatched.take(6).map { movie ->
            ImageRequest.Builder(context)
                .data(BrewImageUrl.forCard(movie.posterUri))
                .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
                .build()
        }
        (avatarRequests + cardRequests).forEach { loader.enqueue(it) }
    }
    val relatedMovies = remember(movieDetails) {
        filterDetailMovies(movieDetails.relatedMovies, movieDetails.id)
    }

    BackHandler {
        when {
            showLanguagesDialog -> showLanguagesDialog = false
            showSynopsisDialog -> showSynopsisDialog = false
            else -> onBackPressed()
        }
    }

    MovieDetailLanguagesDialog(
        showDialog = showLanguagesDialog,
        rows = movieDetails.languageRows,
        onDismissRequest = { showLanguagesDialog = false },
    )
    MovieDetailSynopsisDialog(
        showDialog = showSynopsisDialog,
        title = movieDetails.name,
        synopsis = movieDetails.description,
        onDismissRequest = { showSynopsisDialog = false },
    )

    val scrollDimAlpha by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 0.94f
                else -> (listState.firstVisibleItemScrollOffset / 320f).coerceIn(0f, 0.94f)
            }
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 0.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "showcase") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MovieDetailTokens.DetailShowcaseHeight)
                        .focusGroup()
                        .bringIntoViewIfChildrenAreFocused(),
                ) {
                    MovieDetailShowcaseBackdrop(
                        movieDetails = movieDetails,
                        scrollDimAlpha = scrollDimAlpha,
                        modifier = Modifier.fillMaxSize(),
                    )
                    MovieDetailShowcaseContent(
                        movieDetails = movieDetails,
                        onPrimaryCtaClick = onPrimaryCtaClick,
                        onSecondaryCtaClick = onSecondaryCtaClick,
                        onTrailerClick = onTrailerClick,
                        onShareClick = onShareClick,
                        onOpenLanguages = { showLanguagesDialog = true },
                        isBookmarked = isBookmarked,
                        onBookmarkClick = onBookmarkClick,
                        purchaseLoading = purchaseLoading,
                        reminderSet = reminderSet,
                        modifier = Modifier.fillMaxSize(),
                        primaryFocusRequester = primaryCtaFocusRequester,
                        overlay = {
                            MovieDetailBackButton(
                                onBackPressed = {
                                    when {
                                        showLanguagesDialog -> showLanguagesDialog = false
                                        showSynopsisDialog -> showSynopsisDialog = false
                                        else -> onBackPressed()
                                    }
                                },
                                downFocusRequester = primaryCtaFocusRequester,
                                rightFocusRequester = primaryCtaFocusRequester,
                                modifier = Modifier.align(Alignment.TopStart),
                            )
                        },
                    )
                }
            }

            if (customersAlsoWatched.isNotEmpty()) {
                item(key = "customers_also_watched") {
                    DetailRelatedMoviesRow(
                        title = "Customers Also Watched",
                        movieList = customersAlsoWatched,
                        onMovieSelected = refreshScreenWithNewMovie,
                        contentTopPadding = 4.dp,
                    )
                }
            }

            movieDetails.episodeSeasons.forEach { season ->
                item(key = "season_${season.seasonNo}") {
                    MovieDetailExtrasRow(
                        title = season.title,
                        items = season.episodes,
                        onItemClick = { extra ->
                            extra.vodAssetId?.let { id ->
                                onExtraClick(id, extra.title)
                            }
                        },
                        contentTopPadding = 4.dp,
                    )
                }
            }

            if (movieDetails.bonusClips.isNotEmpty()) {
                item(key = "bonus_clips") {
                    MovieDetailExtrasRow(
                        title = "Bonus Clips",
                        items = movieDetails.bonusClips,
                        onItemClick = { extra ->
                            extra.vodAssetId?.let { id ->
                                onExtraClick(id, extra.title)
                            }
                        },
                        contentTopPadding = 4.dp,
                    )
                }
            }

            if (movieDetails.criticReviews.isNotEmpty()) {
                item(key = "critic_reviews") {
                    CriticReviewsRow(
                        reviews = movieDetails.criticReviews,
                        onReviewClick = { review ->
                            review.link?.takeIf { it.isNotBlank() }?.let { onCriticReviewClick(it) }
                        },
                    )
                }
            }

            if (movieDetails.castAndCrew.isNotEmpty()) {
                item(key = "cast") {
                    CastAndCrewList(
                        castAndCrew = movieDetails.castAndCrew,
                        onCastMemberClick = onCastMemberClick,
                    )
                }
            }

            if (movieDetails.reviewsAndRatings.isNotEmpty() || movieDetails.reviewSummary != null) {
                item(key = "reviews") {
                    UserReviewsRow(
                        reviews = movieDetails.reviewsAndRatings,
                        summary = movieDetails.reviewSummary,
                        userCountry = movieDetails.userCountry,
                    )
                }
            }

            if (relatedMovies.isNotEmpty()) {
                item(key = "related_and_footer") {
                    MovieDetailBottomSection(
                        relatedMovies = relatedMovies,
                        onMovieSelected = refreshScreenWithNewMovie,
                    )
                }
            } else {
                item(key = "detail_footer") {
                    MovieDetailBottomSection()
                }
            }
        }
    }
}

@Composable
private fun MovieDetailBottomSection(
    relatedMovies: MovieList = emptyList(),
    onMovieSelected: (Movie) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
            if (relatedMovies.isNotEmpty()) {
                DetailRelatedMoviesRow(
                    title = "Related Movies",
                    movieList = relatedMovies,
                    onMovieSelected = onMovieSelected,
                    modifier = Modifier.padding(bottom = 28.dp),
                )
            }
            MovieDetailPageFooter()
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.42f to Color(0xFFE8EAED).copy(alpha = 0.10f),
                                0.78f to Color(0xFFD0D4DA).copy(alpha = 0.14f),
                                1f to Color(0xFFB8BEC6).copy(alpha = 0.18f),
                            ),
                        ),
                    )
                },
        )
    }
}
