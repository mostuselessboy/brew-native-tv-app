package com.google.jetstream.data.entities

data class MovieAward(
    val name: String,
    val category: String,
    val year: String,
    val logoUrl: String?,
)

/** Brew critic quote card — mirrors vod-frontend `CriticReview`. */
data class MovieCriticReview(
    val id: String,
    val quote: String,
    val author: String,
    val orgName: String,
    val orgLogoUrl: String?,
    val dateLabel: String?,
    val link: String?,
)

data class MovieDetails(
    val id: String,
    val videoUri: String,
    val subtitleUri: String?,
    val posterUri: String,
    val name: String,
    val description: String,
    val pgRating: String,
    val releaseDate: String,
    val categories: List<String>,
    val duration: String,
    val director: String,
    val screenplay: String,
    val music: String,
    val castAndCrew: List<MovieCast>,
    val awards: List<MovieAward> = emptyList(),
    val criticReviews: List<MovieCriticReview> = emptyList(),
    val status: String,
    val originalLanguage: String,
    val budget: String,
    val revenue: String,
    val similarMovies: MovieList,
    val reviewsAndRatings: List<MovieReviewsAndRatings> = emptyList(),
)
