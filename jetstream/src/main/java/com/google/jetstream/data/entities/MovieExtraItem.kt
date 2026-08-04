package com.google.jetstream.data.entities

/** Bonus clip, episode, or other VOD extra on the movie detail page. */
data class MovieExtraItem(
    val id: String,
    val title: String,
    val thumbnailUri: String,
    val vodAssetId: Int?,
    val subtitle: String? = null,
)

data class MovieEpisodeSeason(
    val seasonNo: Int,
    val title: String,
    val episodes: List<MovieExtraItem>,
)
