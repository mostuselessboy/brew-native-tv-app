package com.google.jetstream.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.google.jetstream.presentation.common.ShowcaseHeight
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.presentation.common.DetailsShimmerSkeleton
import com.google.jetstream.presentation.common.Error
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    LaunchedEffect(movieDetailsScreenViewModel) {
        movieDetailsScreenViewModel.navigateToPlayer.collect { movieId ->
            goToMoviePlayer(movieId)
        }
    }

    when (val s = uiState) {
        is MovieDetailsScreenUiState.Loading -> {
            BackHandler(onBack = onBackPressed)
            DetailsShimmerSkeleton(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Error -> {
            BackHandler(onBack = onBackPressed)
            Error(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Done -> {
            val isBookmarked = (bookmarkState as? BookmarkUiState.Ready)?.isBookmarked == true
            Details(
                movieDetails = s.movieDetails,
                onPrimaryCtaClick = {
                    movieDetailsScreenViewModel.onPrimaryCtaClick(s.movieDetails, s.checkPurchase)
                },
                onSecondaryCtaClick = {
                    movieDetailsScreenViewModel.onSecondaryCtaClick(s.movieDetails, s.checkPurchase)
                },
                onTrailerClick = {
                    movieDetailsScreenViewModel.onTrailerClick(s.movieDetails)
                },
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                isBookmarked = isBookmarked,
                onBookmarkClick = { movieDetailsScreenViewModel.toggleBookmark(s.movieDetails) },
                modifier = Modifier.fillMaxSize(),
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
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showSynopsisDialog by remember { mutableStateOf(false) }
    val firstSectionFocusRequester = remember { FocusRequester() }
    val backFocusRequester = remember { FocusRequester() }
    val primaryCtaFocusRequester = remember { FocusRequester() }
    val secondaryActionsFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val customersAlsoWatched = remember(movieDetails) {
        filterDetailMovies(movieDetails.customersAlsoWatched, movieDetails.id)
    }
    val relatedMovies = remember(movieDetails) {
        filterDetailMovies(movieDetails.relatedMovies, movieDetails.id)
    }

    val hasPurchaseCtas = remember(movieDetails) {
        DetailPurchaseCta.slots(movieDetails).isNotEmpty()
    }

    val firstTrayLazyIndex = remember(movieDetails) {
        firstFocusableSectionLazyIndex(movieDetails) ?: 1
    }

    var boundaryScrollJob by remember { mutableStateOf<Job?>(null) }

    val scrollToShowcase = {
        boundaryScrollJob?.cancel()
        boundaryScrollJob = scope.launch {
            listState.scrollToItem(0, scrollOffset = 0)
        }
    }

    val scrollToFirstTray = {
        boundaryScrollJob?.cancel()
        boundaryScrollJob = scope.launch {
            listState.scrollToItem(firstTrayLazyIndex, scrollOffset = 0)
        }
    }

    LaunchedEffect(movieDetails.id, hasPurchaseCtas) {
        listState.scrollToItem(0, scrollOffset = 0)
        delay(150)
        val target = if (hasPurchaseCtas) {
            primaryCtaFocusRequester
        } else {
            secondaryActionsFocusRequester
        }
        runCatching { target.requestFocus() }
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

    val backDownFocusRequester = if (hasPurchaseCtas) {
        primaryCtaFocusRequester
    } else {
        secondaryActionsFocusRequester
    }
    val heroUpFocusRequester = secondaryActionsFocusRequester

    Box(
        modifier = modifier.background(Color.Black),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 56.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "showcase") {
                MovieDetailShowcaseHero(
                    movieDetails = movieDetails,
                    onPrimaryCtaClick = onPrimaryCtaClick,
                    onSecondaryCtaClick = onSecondaryCtaClick,
                    onTrailerClick = onTrailerClick,
                    onOpenLanguages = { showLanguagesDialog = true },
                    downFocusRequester = firstSectionFocusRequester,
                    primaryCtaFocusRequester = primaryCtaFocusRequester,
                    secondaryActionsFocusRequester = secondaryActionsFocusRequester,
                    upFocusRequester = backFocusRequester,
                    isBookmarked = isBookmarked,
                    onBookmarkClick = onBookmarkClick,
                    onShowcaseActionsFocused = scrollToShowcase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ShowcaseHeight)
                        .focusGroup()
                        .bringIntoViewIfChildrenAreFocused(),
                    overlay = {
                        MovieDetailBackButton(
                            onBackPressed = {
                                when {
                                    showLanguagesDialog -> showLanguagesDialog = false
                                    showSynopsisDialog -> showSynopsisDialog = false
                                    else -> onBackPressed()
                                }
                            },
                            focusRequester = backFocusRequester,
                            downFocusRequester = backDownFocusRequester,
                            rightFocusRequester = backDownFocusRequester,
                            requestInitialFocus = false,
                            modifier = Modifier.align(Alignment.TopStart),
                        )
                    },
                )
            }

            if (customersAlsoWatched.isNotEmpty()) {
                item(key = "customers_also_watched") {
                    DetailRelatedMoviesRow(
                        title = "Customers Also Watched",
                        movieList = customersAlsoWatched,
                        onMovieSelected = refreshScreenWithNewMovie,
                        firstItemFocusRequester = firstSectionFocusRequester,
                        upFocusRequester = heroUpFocusRequester,
                        onFirstItemFocused = scrollToFirstTray,
                        contentTopPadding = 2.dp,
                    )
                }
            }

            if (movieDetails.criticReviews.isNotEmpty()) {
                item(key = "critic_reviews") {
                    CriticReviewsRow(
                        reviews = movieDetails.criticReviews,
                        firstItemFocusRequester = if (customersAlsoWatched.isEmpty()) {
                            firstSectionFocusRequester
                        } else {
                            null
                        },
                        upFocusRequester = if (customersAlsoWatched.isEmpty()) {
                            heroUpFocusRequester
                        } else {
                            null
                        },
                        onFirstItemFocused = if (customersAlsoWatched.isEmpty()) {
                            scrollToFirstTray
                        } else {
                            {}
                        },
                    )
                }
            }

            if (movieDetails.castAndCrew.isNotEmpty()) {
                item(key = "cast") {
                    CastAndCrewList(
                        castAndCrew = movieDetails.castAndCrew,
                        firstItemFocusRequester = if (
                            customersAlsoWatched.isEmpty() &&
                            movieDetails.criticReviews.isEmpty()
                        ) {
                            firstSectionFocusRequester
                        } else {
                            null
                        },
                        upFocusRequester = if (
                            customersAlsoWatched.isEmpty() &&
                            movieDetails.criticReviews.isEmpty()
                        ) {
                            heroUpFocusRequester
                        } else {
                            null
                        },
                        onFirstItemFocused = if (
                            customersAlsoWatched.isEmpty() &&
                            movieDetails.criticReviews.isEmpty()
                        ) {
                            scrollToFirstTray
                        } else {
                            {}
                        },
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
                item(key = "related_movies") {
                    DetailRelatedMoviesRow(
                        title = "Related Movies",
                        movieList = relatedMovies,
                        onMovieSelected = refreshScreenWithNewMovie,
                    )
                }
            }
        }
    }
}
