package com.google.jetstream.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.StringConstants
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
    movieDetailsScreenViewModel: MovieDetailsScreenViewModel = hiltViewModel()
) {
    val uiState by movieDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is MovieDetailsScreenUiState.Loading -> {
            DetailsShimmerSkeleton(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Done -> {
            Details(
                movieDetails = s.movieDetails,
                goToMoviePlayer = { goToMoviePlayer(s.movieDetails.id) },
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun Details(
    movieDetails: MovieDetails,
    goToMoviePlayer: () -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackPressed)
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = modifier,
    ) {
        item {
            MovieDetails(
                movieDetails = movieDetails,
                goToMoviePlayer = goToMoviePlayer,
            )
        }

        if (movieDetails.awards.isNotEmpty()) {
            item {
                AwardsFestivalsRow(
                    awards = movieDetails.awards,
                    modifier = Modifier.padding(top = 28.dp),
                )
            }
        }

        if (movieDetails.castAndCrew.isNotEmpty()) {
            item {
                CastAndCrewList(castAndCrew = movieDetails.castAndCrew)
            }
        }

        if (movieDetails.criticReviews.isNotEmpty()) {
            item {
                CriticReviewsRow(reviews = movieDetails.criticReviews)
            }
        }

        if (movieDetails.similarMovies.isNotEmpty()) {
            item {
                MoviesRow(
                    title = StringConstants
                        .Composable
                        .movieDetailsScreenSimilarTo(movieDetails.name),
                    titleStyle = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = (-0.4).sp,
                    ),
                    movieList = movieDetails.similarMovies,
                    onMovieSelected = refreshScreenWithNewMovie,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}
