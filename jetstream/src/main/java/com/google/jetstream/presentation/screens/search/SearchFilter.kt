package com.google.jetstream.presentation.screens.search

import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList

/**
 * Search result filter tabs shown above the results grid.
 * [vodTagMatch] is the lowercase vod_tag keyword to match in Movie.vodTagLabel.
 * null means the filter uses a different check (Awards uses isFestivalTag).
 */
enum class SearchFilter(val label: String, val vodTagMatch: String?) {
    All("All", null),
    New("New", "new"),
    Trending("Trending", "trending"),
    Hot("Hot", "hot"),
    Awards("Awards", null),
}

/** Apply a SearchFilter to a movie list. All = no-op. */
fun MovieList.applySearchFilter(filter: SearchFilter): MovieList = when (filter) {
    SearchFilter.All -> this
    SearchFilter.Awards -> filter { movie ->
        movie.isFestivalTag ||
            movie.vodTagLabel?.contains("award", ignoreCase = true) == true ||
            movie.vodTagLabel?.contains("cannes", ignoreCase = true) == true ||
            movie.vodTagLabel?.contains("oscar", ignoreCase = true) == true
    }
    else -> filter { movie ->
        movie.vodTagLabel?.contains(filter.vodTagMatch ?: "", ignoreCase = true) == true
    }
}
