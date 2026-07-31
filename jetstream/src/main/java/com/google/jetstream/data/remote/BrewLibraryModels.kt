package com.google.jetstream.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BrewWrappedResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
)

@Serializable
data class BrewMyLibraryResponse(
    val rows: List<BrewMyLibraryRowDto> = emptyList(),
    val meta: BrewMyLibraryMetaDto? = null,
)

@Serializable
data class BrewMyLibraryMetaDto(
    @SerialName("has_active_subscription") val hasActiveSubscription: Boolean = false,
    @SerialName("defaults_applied") val defaultsApplied: Boolean = false,
)

@Serializable
data class BrewMyLibraryRowDto(
    val id: String = "",
    val items: List<JsonElement> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class BrewMyLibraryRequest(
    val shelves: List<BrewMyLibraryShelfRequest> = emptyList(),
)

@Serializable
data class BrewMyLibraryShelfRequest(
    val id: String,
    val limit: Int = 10,
    val offset: Int = 0,
)

@Serializable
data class BrewLibraryOrderDto(
    val id: Int = 0,
    @SerialName("purchase_type") val purchaseType: String? = null,
    @SerialName("access_type") val accessType: String? = null,
    @SerialName("watch_progress") val watchProgress: BrewLibraryWatchProgressDto? = null,
    @SerialName("campaign_info") val campaignInfo: BrewLibraryCampaignInfoDto? = null,
    @SerialName("click_action") val clickAction: String? = null,
)

@Serializable
data class BrewLibraryWatchProgressDto(
    @SerialName("percentage_watched") val percentageWatched: Double = 0.0,
    @SerialName("initial_time_seconds") val initialTimeSeconds: Double = 0.0,
    @SerialName("duration_seconds") val durationSeconds: Double? = null,
    @SerialName("credits_start_time") val creditsStartTime: Double? = null,
    @SerialName("finish_content_at") val finishContentAt: String? = null,
    @SerialName("asset_type") val assetType: String? = null,
    @SerialName("is_up_next") val isUpNext: Boolean? = null,
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    @SerialName("episode_no") val episodeNo: Int? = null,
    @SerialName("season_no") val seasonNo: Int? = null,
    @SerialName("asset_title") val assetTitle: String? = null,
    @SerialName("video_settings") val videoSettings: BrewLibraryVideoSettingsDto? = null,
)

@Serializable
data class BrewLibraryVideoSettingsDto(
    @SerialName("initialTime") val initialTime: Double? = null,
    @SerialName("percentage_watched") val percentageWatched: Double? = null,
    @SerialName("is_up_next") val isUpNext: Boolean? = null,
)

@Serializable
data class BrewLibraryCampaignInfoDto(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("localized_cv_name") val localizedCvName: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("project_type") val projectType: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("project_synopsis") val projectSynopsis: String? = null,
    @SerialName("genre") val genre: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("is_svod") val isSvod: Boolean? = null,
    @SerialName("is_tvod") val isTvod: Boolean? = null,
    @SerialName("is_store_content") val isStoreContent: Boolean? = null,
    val appearance: BrewLibraryAppearanceDto? = null,
)

@Serializable
data class BrewLibraryAppearanceDto(
    @SerialName("vertical_background_art") val verticalBackgroundArt: JsonElement? = null,
    @SerialName("vertical_thumbnails") val verticalThumbnails: JsonElement? = null,
    @SerialName("horizontal_thumbnails") val horizontalThumbnails: JsonElement? = null,
    @SerialName("background_art") val backgroundArt: JsonElement? = null,
)

@Serializable
data class BrewBookmarkItemDto(
    val id: Int = 0,
    @SerialName("user_id") val userId: Int = 0,
    @SerialName("vod_asset_id") val vodAssetId: Int = 0,
    @SerialName("asset_title") val assetTitle: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_type") val projectType: String? = null,
    val slug: String? = null,
    @SerialName("project_poster_url") val projectPosterUrl: String? = null,
    @SerialName("episode_no") val episodeNo: String? = null,
    @SerialName("season_no") val seasonNo: String? = null,
    @SerialName("campaign_info") val campaignInfo: BrewLibraryCampaignInfoDto? = null,
)

@Serializable
data class BrewBookmarkStatusDto(
    @SerialName("is_bookmarked") val isBookmarked: Boolean = false,
    @SerialName("bookmark_id") val bookmarkId: Int? = null,
)

@Serializable
data class BrewAddBookmarkRequest(
    @SerialName("user_id") val userId: Int,
    @SerialName("vod_asset_id") val vodAssetId: Int,
)
