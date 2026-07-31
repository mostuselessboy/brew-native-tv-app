package com.google.jetstream.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BrewCheckPurchaseResponse(
    @SerialName("allowed_in_region") val allowedInRegion: Boolean? = null,
    @SerialName("has_purchased") val hasPurchased: Boolean = false,
    @SerialName("has_rented") val hasRented: Boolean = false,
    @SerialName("has_subscription") val hasSubscription: Boolean? = null,
    @SerialName("has_active_subscription") val hasActiveSubscription: Boolean? = null,
    @SerialName("has_free_playback_access") val hasFreePlaybackAccess: Boolean? = null,
    @SerialName("is_own_film") val isOwnFilm: Boolean = false,
    @SerialName("is_bookmarked") val isBookmarked: Boolean? = null,
    @SerialName("video_settings") val videoSettings: BrewCheckPurchaseVideoSettings? = null,
    @SerialName("purchase_details") val purchaseDetails: BrewCheckPurchaseDetails? = null,
    @SerialName("purchase_cta") val purchaseCta: BrewPurchaseCtaDto? = null,
    @SerialName("subscription_plans") val subscriptionPlans: List<BrewSubscriptionPlanDto> = emptyList(),
)

@Serializable
data class BrewCheckPurchaseVideoSettings(
    @SerialName("vod_asset_id") val vodAssetId: JsonElement? = null,
    @SerialName("initialTime") val initialTime: JsonElement? = null,
    @SerialName("initial_time") val initialTimeAlt: JsonElement? = null,
    @SerialName("percentage_watched") val percentageWatched: JsonElement? = null,
    @SerialName("season_no") val seasonNo: Int? = null,
    @SerialName("episode_no") val episodeNo: Int? = null,
)

@Serializable
data class BrewCheckPurchaseDetails(
    @SerialName("first_playback_at") val firstPlaybackAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("purchase_type") val purchaseType: String? = null,
)

@Serializable
data class BrewStartPlaybackRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("campaign_version_id") val campaignVersionId: Int? = null,
)

@Serializable
data class BrewVodAssetData(
    @SerialName("bunny_video_id") val bunnyVideoId: String = "",
    @SerialName("bunny_cdn_zone") val bunnyCdnZone: String = "",
    @SerialName("bunny_library_id") val bunnyLibraryId: String = "",
    @SerialName("is_drm") val isDrm: Boolean = false,
    @SerialName("license_server_url") val licenseServerUrl: String? = null,
    @SerialName("fairplay_certificate_url") val fairplayCertificateUrl: String? = null,
    val token: String? = null,
    val title: String? = null,
    @SerialName("has_access") val hasAccess: Boolean = false,
    @SerialName("allowed_in_region") val allowedInRegion: Boolean = true,
    val progress: BrewVodAssetProgress? = null,
    @SerialName("credits_start_time") val creditsStartTime: JsonElement? = null,
)

@Serializable
data class BrewVodAssetProgress(
    @SerialName("current_time") val currentTime: Double = 0.0,
    val duration: Double = 0.0,
)

@Serializable
data class BrewEndscreenRecommendationsResponse(
    val flow: String? = null,
    @SerialName("primary_card") val primaryCard: BrewEndscreenRecommendationCard? = null,
    @SerialName("secondary_card") val secondaryCard: BrewEndscreenRecommendationCard? = null,
)

@Serializable
data class BrewEndscreenRecommendationCard(
    @SerialName("card_data") val cardData: BrewEndscreenCardData? = null,
    val action: String? = null,
    @SerialName("card_origin") val cardOrigin: String? = null,
)

@Serializable
data class BrewEndscreenCardData(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("localized_cv_name") val localizedCvName: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("project_type") val projectType: String? = null,
    @SerialName("trailer_url") val trailerUrl: String? = null,
    @SerialName("movie_asset_id") val movieAssetId: Int? = null,
    @SerialName("is_free_tier") val isFreeTier: Boolean? = null,
)
