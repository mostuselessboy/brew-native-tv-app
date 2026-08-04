package com.google.jetstream.data.util

import com.google.jetstream.data.entities.MovieDetails

/** brew.tv web URLs for TV QR flows — mirrors vod-frontend share / checkout links. */
object BrewWebUrls {

    private const val BASE = "https://www.brew.tv"

    fun movieSlug(movie: MovieDetails): String =
        movie.cvName.takeIf { it.isNotBlank() } ?: movie.id

    fun moviePage(movie: MovieDetails): String =
        "$BASE/${movieSlug(movie)}"

    fun shareSlug(slug: String): String = "$BASE/${slug.trim('/')}"

    fun share(movie: MovieDetails): String = moviePage(movie)

    fun rent(movie: MovieDetails): String = moviePage(movie)

    fun buy(movie: MovieDetails): String = moviePage(movie)

    fun subscribeYearly(): String = "$BASE/plus"

    fun subscribeQuarterly(): String = "$BASE/plus"

    fun displayPath(url: String): String =
        url.removePrefix("https://").removePrefix("http://").trimEnd('/')

    fun isYoutube(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.contains("youtube.com", ignoreCase = true) ||
            url.contains("youtu.be", ignoreCase = true)
    }
}
