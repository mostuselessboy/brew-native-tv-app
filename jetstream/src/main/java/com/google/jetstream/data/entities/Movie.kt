package com.google.jetstream.data.entities

import com.google.jetstream.data.util.LibraryClickAction

import com.google.jetstream.data.models.MoviesResponseItem

data class Movie(
    val id: String,
    val videoUri: String,
    val subtitleUri: String?,
    val posterUri: String,
    /** Alternate landscape for hero/backdrop — different daily pick than [posterUri]. */
    val heroBackdropUri: String? = null,
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
    val ribbonLabel: String? = null,
    val projectType: String? = null,
    val isComingSoon: Boolean = false,
    val isFreeTier: Boolean = false,
    val rentPriceFormatted: String? = null,
    val comingSoonHint: String? = null,
    /** Numeric campaign id — used for showcase reminder_set_ids. */
    val campaignId: Int? = null,
    val campaignVersionId: Int? = null,
    /** Continue-watching progress (0–100) for tray / library cards. */
    val watchProgressPercent: Int? = null,
    /** VOD asset for direct resume playback from library / CW trays. */
    val vodAssetId: Int? = null,
    val initialTimeSeconds: Int? = null,
    val libraryClickAction: LibraryClickAction? = null,
)

fun MoviesResponseItem.toMovie(thumbnailType: ThumbnailType = ThumbnailType.Standard): Movie {
    val thumbnail = when (thumbnailType) {
        ThumbnailType.Standard -> image_2_3
        ThumbnailType.Long -> image_16_9
    }
    return Movie(
        id = id,
        videoUri = videoUri,
        subtitleUri = subtitleUri,
        posterUri = thumbnail,
        name = title,
        description = fullTitle,
    )
}

enum class ThumbnailType {
    Standard,
    Long
}
