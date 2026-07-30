package com.google.jetstream.data.entities

data class HomeSection(
    val id: String,
    val title: String,
    val type: HomeSectionType,
    val movies: MovieList,
    val subheading: String? = null,
    /** Prime/Brew "Most Watched" style — large rank numerals beside cards. */
    val showRanking: Boolean = false,
)

enum class HomeSectionType {
    Showcase,
    Immersive,
    Row,
    RandomMoviePicker,
}
