package com.google.jetstream.data.entities

import com.google.jetstream.data.models.MoviesResponseItem

data class Movie(
    val id: String,
    val videoUri: String,
    val subtitleUri: String?,
    val posterUri: String,
    val name: String,
    /** Sales pitch (`short_description`) when present; otherwise empty. */
    val description: String,
    val year: String? = null,
    val country: String? = null,
    val genres: List<String> = emptyList(),
    val duration: String? = null,
    /** White corner badge label from `vod_tag` (New / Trending / Cannes…). */
    val vodTagLabel: String? = null,
    val isFestivalTag: Boolean = false,
    val showStore: Boolean = false,
    val showBrewPlus: Boolean = false,
    val leavingSoon: Boolean = false,
    /** Landscape backdrop for detail related rails — mobile-viewer `backdropImage`. */
    val backdropUri: String? = null,
)

fun MoviesResponseItem.toMovie(thumbnailType: ThumbnailType = ThumbnailType.Standard): Movie {
    val thumbnail = when (thumbnailType) {
        ThumbnailType.Standard -> image_2_3
        ThumbnailType.Long -> image_16_9
    }
    return Movie(
        id,
        videoUri,
        subtitleUri,
        thumbnail,
        title,
        fullTitle
    )
}

enum class ThumbnailType {
    Standard,
    Long
}
