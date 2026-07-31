package com.google.jetstream.presentation.screens.movies

import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieDetails

internal fun filterDetailMovies(
    movies: List<Movie>,
    excludeId: String,
): List<Movie> =
    movies
        .distinctBy { it.id }
        .filter { it.id.isNotBlank() && it.id != excludeId }
        .take(12)

/** LazyColumn index of first tray below showcase (showcase = item 0). */
internal fun firstFocusableSectionLazyIndex(movieDetails: MovieDetails): Int? {
    var index = 1
    if (filterDetailMovies(movieDetails.customersAlsoWatched, movieDetails.id).isNotEmpty()) {
        return index
    }
    if (movieDetails.criticReviews.isNotEmpty()) return index
    if (movieDetails.castAndCrew.isNotEmpty()) return index
    if (movieDetails.reviewsAndRatings.isNotEmpty() || movieDetails.reviewSummary != null) {
        return index
    }
    if (filterDetailMovies(movieDetails.relatedMovies, movieDetails.id).isNotEmpty()) return index
    return null
}
