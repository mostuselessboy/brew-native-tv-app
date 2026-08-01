package com.google.jetstream.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.common.BrewQrPopup
import com.google.jetstream.presentation.common.DetailsShimmerSkeleton
import com.google.jetstream.presentation.common.Error

object MovieDetailsScreen {
    const val MovieIdBundleKey = "movieId"
}

@Composable
fun MovieDetailsScreen(
    goToMoviePlayer: (movieId: String) -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    movieDetailsScreenViewModel: MovieDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by movieDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    val bookmarkState by movieDetailsScreenViewModel.bookmarkState.collectAsStateWithLifecycle()
    val qrPopup by movieDetailsScreenViewModel.qrPopup.collectAsStateWithLifecycle()

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
            BackHandler(enabled = qrPopup != null) {
                movieDetailsScreenViewModel.dismissQrPopup()
            }
            Details(
                movieDetails = s.movieDetails,
                onPrimaryCtaClick = onPrimaryCtaClick,
                onSecondaryCtaClick = onSecondaryCtaClick,
                onTrailerClick = onTrailerClick,
                onShareClick = onShareClick,
                onCriticReviewClick = onCriticReviewClick,
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                isBookmarked = isBookmarked,
                onBookmarkClick = onBookmarkClick,
                onExtraClick = onExtraClick,
                modifier = Modifier.fillMaxSize(),
            )
            BrewQrPopup(
                state = qrPopup,
                onDismissRequest = movieDetailsScreenViewModel::dismissQrPopup,
                onDone = movieDetailsScreenViewModel::onQrPopupDone,
            )
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
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    onExtraClick: (vodAssetId: Int, title: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showSynopsisDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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
                        .height(MovieDetailTokens.DetailShowcaseHeight),
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
                        modifier = Modifier.fillMaxSize(),
                        overlay = {
                            MovieDetailBackButton(
                                onBackPressed = {
                                    when {
                                        showLanguagesDialog -> showLanguagesDialog = false
                                        showSynopsisDialog -> showSynopsisDialog = false
                                        else -> onBackPressed()
                                    }
                                },
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black,
                            0.22f to Color.Black,
                            0.55f to Color(0xFF0C0A08),
                            0.82f to Color(0xFF141008),
                            1f to Color(0xFF1A140C),
                        ),
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF7A2E).copy(alpha = 0.10f),
                            Color(0xFFFF9A4D).copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.92f),
                        radius = size.width * 0.65f,
                    ),
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (relatedMovies.isNotEmpty()) {
                DetailRelatedMoviesRow(
                    title = "Related Movies",
                    movieList = relatedMovies,
                    onMovieSelected = onMovieSelected,
                )
            }
            MovieDetailPageFooter()
        }
    }
}
