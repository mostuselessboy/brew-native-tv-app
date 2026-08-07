package com.google.jetstream.data.entities

import com.google.jetstream.data.util.DetailPurchaseCtaSlot

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

data class MovieLanguageRow(
    val key: String,
    val displayName: String,
    val hasAudio: Boolean,
    val hasSubtitles: Boolean,
)

data class MovieDetails(
    val id: String,
    val videoUri: String,
    val subtitleUri: String?,
    val posterUri: String,
    val portraitPosterUri: String? = null,
    val name: String,
    val description: String,
    val tagline: String = "",
    val pgRating: String,
    val releaseDate: String,
    val categories: List<String>,
    val duration: String,
    val releaseYear: String = "",
    val imdbLink: String = "",
    val letterboxdLink: String = "",
    val rottenTomatoesLink: String = "",
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
    /** Viewer average on 0–10 scale (display as /5). */
    val averageRating: Double? = null,
    val ratingCount: Int? = null,
    val customersAlsoWatched: MovieList = emptyList(),
    val relatedMovies: MovieList = emptyList(),
    val reviewsAndRatings: List<MovieReviewsAndRatings> = emptyList(),
    val reviewSummary: MovieReviewSummary? = null,
    val userCountry: String = "",
    // Commerce — mirrors mobile-viewer purchase CTA inputs
    val projectType: String? = null,
    val isComingSoon: Boolean = false,
    val comingSoonHint: String? = null,
    val isFreeTier: Boolean = false,
    val showBrewPlus: Boolean = false,
    val showStore: Boolean = false,
    val rentPriceFormatted: String? = null,
    val buyPriceFormatted: String? = null,
    val rentOriginalPriceFormatted: String? = null,
    val buyOriginalPriceFormatted: String? = null,
    val languageRows: List<MovieLanguageRow> = emptyList(),
    val hasTrailer: Boolean = false,
    val trailerVodAssetId: Int? = null,
    val trailerIsDrm: Boolean = false,
    val trailerOriginalUrl: String? = null,
    val trailerIsYoutube: Boolean = false,
    val trailerIsPublic: Boolean = false,
    val subscriptionPlans: List<MovieSubscriptionPlan> = emptyList(),
    val ribbonLabel: String? = null,
    /** Enriched from get-campaign `purchase_cta.slots` when present. */
    val purchaseCtaSlots: List<DetailPurchaseCtaSlot> = emptyList(),
    /** Main VOD asset id for bookmark API. */
    val vodAssetId: Int? = null,
    val bonusClips: List<MovieExtraItem> = emptyList(),
    val episodeSeasons: List<MovieEpisodeSeason> = emptyList(),
    /** Slug for check-purchase / start-playback / vod asset APIs. */
    val cvName: String = "",
    val campaignVersionId: Int? = null,
    val campaignId: Int? = null,
)
