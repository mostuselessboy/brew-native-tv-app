package com.google.jetstream.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.presentation.common.DetailsShimmerSkeleton
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.MoviesRow

object MovieDetailsScreen {
    const val MovieIdBundleKey = "movieId"
}

@Composable
fun MovieDetailsScreen(
    goToMoviePlayer: (movieId: String) -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    openAuth: () -> Unit = {},
    movieDetailsScreenViewModel: MovieDetailsScreenViewModel = hiltViewModel(),
    authViewModel: MovieDetailsAuthViewModel = hiltViewModel(),
) {
    val uiState by movieDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is MovieDetailsScreenUiState.Loading -> {
            DetailsShimmerSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }

        is MovieDetailsScreenUiState.Error -> {
            Error(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }

        is MovieDetailsScreenUiState.Done -> {
            Details(
                movieDetails = s.movieDetails,
                goToMoviePlayer = { goToMoviePlayer(s.movieDetails.id) },
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                isLoggedIn = isLoggedIn,
                openAuth = openAuth,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Details(
    movieDetails: MovieDetails,
    goToMoviePlayer: () -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    isLoggedIn: Boolean,
    openAuth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackPressed)
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusGroup(),
    ) {
        item(key = "hero") {
            MovieDetails(
                movieDetails = movieDetails,
                goToMoviePlayer = goToMoviePlayer,
                isLoggedIn = isLoggedIn,
                onLoginRequired = openAuth,
            )
        }

        if (movieDetails.awards.isNotEmpty()) {
            item(key = "awards") {
                AwardsFestivalsRow(
                    awards = movieDetails.awards,
                    modifier = Modifier.padding(top = 28.dp),
                )
            }
        }

        item(key = "details-info") {
            MovieDetailsInfoSection(movieDetails = movieDetails)
        }

        if (movieDetails.castAndCrew.isNotEmpty()) {
            item(key = "cast") {
                CastAndCrewList(castAndCrew = movieDetails.castAndCrew)
            }
        }

        if (movieDetails.criticReviews.isNotEmpty()) {
            item(key = "critics") {
                CriticReviewsRow(reviews = movieDetails.criticReviews)
            }
        }

        if (movieDetails.alsoWatchedMovies.isNotEmpty()) {
            item(key = "also-watched") {
                MoviesRow(
                    title = "Customers also watched",
                    titleStyle = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = (-0.4).sp,
                    ),
                    movieList = movieDetails.alsoWatchedMovies,
                    onMovieSelected = refreshScreenWithNewMovie,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }

        if (movieDetails.relatedMovies.isNotEmpty()) {
            item(key = "related") {
                MoviesRow(
                    title = "Related movies",
                    titleStyle = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = (-0.4).sp,
                    ),
                    movieList = movieDetails.relatedMovies,
                    onMovieSelected = refreshScreenWithNewMovie,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}
