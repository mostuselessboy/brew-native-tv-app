/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class BrewHomeSectionResponse(
    val success: Boolean = false,
    val data: BrewHomeSectionDto? = null,
)

@Serializable
data class BrewHomeSectionDto(
    val id: Int,
    val position: Int = 0,
    val type: String = "",
    val name: String = "",
    val subheading: String? = null,
    val slug: String? = null,
    val metadata: BrewSectionMetadataDto? = null,
    val content: List<BrewHomeContentItemDto> = emptyList(),
    val total: Int? = null,
    @SerialName("page_size") val pageSize: Int? = null,
    val page: Int? = null,
)

@Serializable
data class BrewSectionMetadataDto(
    @SerialName("show_ranking") val showRanking: Boolean? = null,
    @SerialName("card_style") val cardStyle: String? = null,
    @SerialName("pinned_till") val pinnedTill: Int? = null,
)

@Serializable
data class BrewHomeContentItemDto(
    val position: Int = 0,
    val cid: Int? = null,
    @SerialName("content_data") val contentData: BrewContentDataDto? = null,
)

@Serializable
data class BrewContentDataDto(
    val id: Int? = null,
    @SerialName("project_id") val projectId: Int? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_synopsis") val projectSynopsis: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    val slug: String? = null,
    val genres: List<String> = emptyList(),
    val runtime: String? = null,
    @SerialName("project_type") val projectType: String? = null,
    @SerialName("background_art_url") val backgroundArtUrl: String? = null,
    @SerialName("vertical_background_art_url") val verticalBackgroundArtUrl: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("trailer_original_url") val trailerOriginalUrl: String? = null,
    @SerialName("content_rating_label") val contentRatingLabel: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("horizontal_thumbnails") val horizontalThumbnails: List<String> = emptyList(),
    @SerialName("vertical_thumbnails") val verticalThumbnails: List<String> = emptyList(),
    val cast: List<BrewHomeCastDto> = emptyList(),
    val country: String? = null,
    @SerialName("vod_tag") val vodTag: String? = null,
    @SerialName("ribbon_label") val ribbonLabel: String? = null,
    @SerialName("leave_date") val leaveDate: String? = null,
    @SerialName("monetization_model") val monetizationModel: List<String> = emptyList(),
    @SerialName("is_svod") val isSvod: Boolean? = null,
    @SerialName("is_tvod") val isTvod: Boolean? = null,
    @SerialName("is_store_content") val isStoreContent: Boolean? = null,
    @SerialName("available_for_buy") val availableForBuy: Boolean? = null,
    @SerialName("available_for_rent") val availableForRent: Boolean? = null,
    @SerialName("pricing_data") val pricingData: BrewPricingDataDto? = null,
    @SerialName("localized_cv_name") val localizedCvName: String? = null,
    @SerialName("canonical_cv_name") val canonicalCvName: String? = null,
    @SerialName("is_free_tier") val isFreeTier: Boolean? = null,
    @SerialName("rent_price_formatted") val rentPriceFormatted: String? = null,
    val distribution: String? = null,
    @SerialName("coming_soon_release_info") val comingSoonReleaseInfo: BrewComingSoonReleaseInfoDto? = null,
)

@Serializable
data class BrewComingSoonReleaseInfoDto(
    @SerialName("vod_release_timestamp") val vodReleaseTimestamp: String? = null,
)

@Serializable
data class BrewPricingDataDto(
    @SerialName("pricing_id") val pricingId: Int = 0,
    @Serializable(with = PricingOptionListSerializer::class)
    val buy: List<BrewPricingOptionDto> = emptyList(),
    @Serializable(with = PricingOptionListSerializer::class)
    val rent: List<BrewPricingOptionDto> = emptyList(),
    @SerialName("viewer_release_option") val viewerReleaseOption: String? = null,
    @SerialName("viewer_monetization_models") val viewerMonetizationModels: Map<String, JsonObject> = emptyMap(),
    @SerialName("is_excluded") val isExcluded: Boolean = false,
)

@Serializable
data class BrewPricingOptionDto(
    val price: Double? = null,
    val currency: String? = null,
    @SerialName("perceived_price") val perceivedPrice: Double? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("currency_symbol") val currencySymbol: String? = null,
)

@Serializable
data class BrewHomeCastDto(
    val id: Int? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val job: String? = null,
    @SerialName("character_name") val characterName: List<String> = emptyList(),
)

@Serializable
data class BrewSubtitleDto(
    @SerialName("language_name") val languageName: String? = null,
    val language: String? = null,
    val name: String? = null,
    val title: String? = null,
    @SerialName("srclang") val srcLang: String? = null,
    val lang: String? = null,
)

@Serializable
data class BrewCampaignResponse(
    val success: Boolean = false,
    val data: BrewCampaignData? = null,
)

@Serializable
data class BrewCampaignData(
    val title: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_synopsis") val projectSynopsis: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("preferred_slug") val preferredSlug: String? = null,
    @SerialName("background_art") val backgroundArt: String? = null,
    @SerialName("vertical_background_art") val verticalBackgroundArt: String? = null,
    @SerialName("runtime_minutes") val runtimeMinutes: Int? = null,
    val genres: List<String> = emptyList(),
    val rating: Double? = null,
    @SerialName("ratingCount") val ratingCount: Int? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val country: String? = null,
    @SerialName("content_rating_label") val contentRatingLabel: String? = null,
    @SerialName("project_trailer")
    @Serializable(with = StringOrStringListSerializer::class)
    val projectTrailer: List<String> = emptyList(),
    @SerialName("cast_and_awards") val castAndAwards: BrewCastAndAwards? = null,
    val collections: List<BrewCollectionDto> = emptyList(),
    @SerialName("critic_reviews") val criticReviews: List<BrewCriticReviewDto> = emptyList(),
    @SerialName("user_reviews") val userReviews: List<BrewUserReviewDto> = emptyList(),
    @SerialName("vod_primary_language") val vodPrimaryLanguage: BrewLanguageDto? = null,
    val subtitles: List<BrewSubtitleDto> = emptyList(),
    @SerialName("project_type") val projectType: String? = null,
    @SerialName("is_svod") val isSvod: Boolean? = null,
    @SerialName("is_tvod") val isTvod: Boolean? = null,
    @SerialName("is_free_tier") val isFreeTier: Boolean? = null,
    @SerialName("is_store_content") val isStoreContent: Boolean? = null,
    @SerialName("available_for_buy") val availableForBuy: Boolean? = null,
    @SerialName("available_for_rent") val availableForRent: Boolean? = null,
    @SerialName("rent_price_formatted") val rentPriceFormatted: String? = null,
    @SerialName("monetization_model") val monetizationModel: List<String> = emptyList(),
    @SerialName("pricing_data") val pricingData: BrewPricingDataDto? = null,
    val distribution: String? = null,
    @SerialName("coming_soon_release_info") val comingSoonReleaseInfo: BrewComingSoonReleaseInfoDto? = null,
    val campaign: BrewCampaignMeta? = null,
    val project: BrewProjectDto? = null,
    val trailer: BrewTrailerDto? = null,
    @SerialName("imdb_url") val imdbUrl: String? = null,
    @SerialName("letterboxd_url") val letterboxdUrl: String? = null,
    @SerialName("letterboxd_link") val letterboxdLink: String? = null,
    @SerialName("rottentomatoes_link") val rottenTomatoesLink: String? = null,
    @SerialName("purchase_cta") val purchaseCta: BrewPurchaseCtaDto? = null,
    @SerialName("ribbon_label") val ribbonLabel: String? = null,
    @SerialName("subscription_plans") val subscriptionPlans: List<BrewSubscriptionPlanDto> = emptyList(),
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    @SerialName("movie_details") val movieDetails: BrewMovieDetailsDto? = null,
    @SerialName("user_country") val userCountry: String? = null,
    @SerialName("allowed_in_region") val allowedInRegion: Boolean? = null,
    @SerialName("is_brew_unavailable") val isBrewUnavailable: Boolean = false,
    @SerialName("bonus_clips") val bonusClips: List<BrewBonusClipDto> = emptyList(),
    @SerialName("series_data") val seriesData: List<BrewSeriesSeasonDto> = emptyList(),
    @SerialName("vod_series_metadata") val vodSeriesMetadata: BrewVodSeriesMetadataDto? = null,
)

@Serializable
data class BrewBonusClipDto(
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
    @SerialName("bonus_clips_type") val bonusClipsType: String? = null,
)

@Serializable
data class BrewSeriesSeasonDto(
    @SerialName("season_no") val seasonNo: Int? = null,
    val episodes: List<BrewEpisodeDto> = emptyList(),
)

@Serializable
data class BrewEpisodeDto(
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    @SerialName("episode_no") val episodeNo: Int? = null,
    @SerialName("season_no") val seasonNo: Int? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
)

@Serializable
data class BrewVodSeriesMetadataDto(
    val seasons: List<BrewSeasonMetadataDto> = emptyList(),
)

@Serializable
data class BrewSeasonMetadataDto(
    val index: Int? = null,
    val name: String? = null,
    val description: String? = null,
)

@Serializable
data class BrewMovieDetailsDto(
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    @SerialName("movie_asset_id") val movieAssetId: Int? = null,
)

@Serializable
data class BrewSubscriptionPlanDto(
    val id: Int = 0,
    val name: String? = null,
    val price: Double = 0.0,
    @SerialName("perceived_price") val perceivedPrice: Double? = null,
    val currency: String? = null,
    @SerialName("currency_symbol") val currencySymbol: String? = null,
    @SerialName("interval_unit") val intervalUnit: String? = null,
    @SerialName("interval_count") val intervalCount: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class BrewPurchaseCtaDto(
    val scenario: String? = null,
    val slots: List<BrewPurchaseCtaSlotDto> = emptyList(),
    @SerialName("add_ons") val addOns: BrewPurchaseCtaAddOnsDto? = null,
)

@Serializable
data class BrewPurchaseCtaSlotDto(
    val kind: String? = null,
    val color: String? = null,
    val free: Boolean? = null,
    @SerialName("reminder_set") val reminderSet: Boolean? = null,
    @SerialName("is_continue_watching") val isContinueWatching: Boolean? = null,
    @SerialName("percentage_watched") val percentageWatched: Int? = null,
    val reason: String? = null,
)

@Serializable
data class BrewPurchaseCtaAddOnsDto(
    val coupons: Boolean = false,
    @SerialName("more_purchase_options") val morePurchaseOptions: Boolean = false,
    val download: Boolean? = null,
)

@Serializable
data class BrewCastAndAwards(
    @SerialName("cast_and_crew") val castAndCrew: List<BrewCastMemberDto> = emptyList(),
    val awards: List<BrewAwardDto> = emptyList(),
)

@Serializable
data class BrewAwardDto(
    val id: Int? = null,
    val name: String? = null,
    val category: String? = null,
    val year: String? = null,
    val logo: String? = null,
    val type: String? = null,
    val anchor: Boolean = false,
)

@Serializable
data class BrewCastMemberDto(
    val id: Int? = null,
    val slug: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    val job: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
)

@Serializable
data class BrewCriticReviewDto(
    val id: Int? = null,
    val author: String? = null,
    val review: String? = null,
    val date: String? = null,
    val link: String? = null,
    @SerialName("org_name") val orgName: String? = null,
    @SerialName("org_logo") val orgLogo: String? = null,
)

@Serializable
data class BrewCollectionDto(
    @SerialName("collection_name") val collectionName: String? = null,
    @SerialName("collection_slug") val collectionSlug: String? = null,
    @SerialName("top_movies") val topMovies: List<BrewRelatedMovieDto> = emptyList(),
)

@Serializable
data class BrewRelatedMovieDto(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("background_art_url") val backgroundArtUrl: String? = null,
    @SerialName("vertical_background_art_url") val verticalBackgroundArtUrl: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
)

@Serializable
data class BrewUserReviewDto(
    val name: String? = null,
    val rating: Double? = null,
    val review: String? = null,
)

@Serializable
data class BrewLanguageDto(
    val name: String? = null,
    @SerialName("iso_code") val isoCode: String? = null,
)

@Serializable
data class BrewCampaignMeta(
    val id: Int? = null,
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("campaign_version_id") val campaignVersionId: Int? = null,
    @SerialName("ribbon_label") val ribbonLabel: String? = null,
    val status: String? = null,
    val distribution: String? = null,
    @SerialName("trailer_original_url") val trailerOriginalUrl: String? = null,
    val appearance: BrewAppearanceDto? = null,
    @SerialName("movie_details") val movieDetails: BrewMovieDetailsDto? = null,
    @SerialName("bonus_clips") val bonusClips: List<BrewBonusClipDto> = emptyList(),
    @SerialName("series_data") val seriesData: List<BrewSeriesSeasonDto> = emptyList(),
    @SerialName("vod_series_metadata") val vodSeriesMetadata: BrewVodSeriesMetadataDto? = null,
)

@Serializable
data class BrewAppearanceDto(
    @SerialName("background_art") val backgroundArt: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("vertical_background_art") val verticalBackgroundArt: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("horizontal_thumbnails") val horizontalThumbnails: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("vertical_thumbnails") val verticalThumbnails: kotlinx.serialization.json.JsonElement? = null,
)

@Serializable
data class BrewAlsoWatchedResponse(
    val success: Boolean = false,
    val data: BrewAlsoWatchedData? = null,
)

@Serializable
data class BrewAlsoWatchedData(
    @SerialName("related_movies") val relatedMovies: List<BrewAlsoWatchedMovieDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)

@Serializable
data class BrewCommentsResponse(
    val success: Boolean = false,
    val data: BrewCommentsData? = null,
)

@Serializable
data class BrewCommentsData(
    val comments: List<BrewCommentDto> = emptyList(),
    val pagination: BrewCommentsPagination? = null,
    @SerialName("averageRating") val averageRating: Double? = null,
    @SerialName("totalRatings") val totalRatings: Int? = null,
    @SerialName("ratingDistribution") val ratingDistribution: Map<String, Int> = emptyMap(),
)

@Serializable
data class BrewCommentsPagination(
    val page: Int = 1,
    val limit: Int = 5,
    val total: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
)

@Serializable
data class BrewCommentDto(
    val id: String? = null,
    val heading: String? = null,
    val text: String? = null,
    @SerialName("star_rating") val starRating: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val section: String? = null,
    val user: BrewCommentUserDto? = null,
)

@Serializable
data class BrewCommentUserDto(
    val id: Int? = null,
    val name: String? = null,
    val image: String? = null,
    val username: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("country_name") val countryName: String? = null,
    @SerialName("is_brew_critic") val isBrewCritic: Boolean? = null,
)

@Serializable
data class BrewAlsoWatchedMovieDto(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val country: String? = null,
    val genres: List<String> = emptyList(),
    @SerialName("vod_tag") val vodTag: String? = null,
    val appearance: kotlinx.serialization.json.JsonObject? = null,
    @SerialName("is_svod") val isSvod: Boolean? = null,
    @SerialName("is_tvod") val isTvod: Boolean? = null,
    @SerialName("is_store_content") val isStoreContent: Boolean? = null,
    @SerialName("available_for_buy") val availableForBuy: Boolean? = null,
    @SerialName("available_for_rent") val availableForRent: Boolean? = null,
    @SerialName("monetization_model") val monetizationModel: List<String> = emptyList(),
)

@Serializable
data class BrewProjectDto(
    val id: Int? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_synopsis") val projectSynopsis: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("preferred_slug") val preferredSlug: String? = null,
    val runtime: Int? = null,
    val genres: List<String> = emptyList(),
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("imdb_link") val imdbLink: String? = null,
    @SerialName("letterboxd_link") val letterboxdLink: String? = null,
    @SerialName("rottentomatoes_link") val rottenTomatoesLink: String? = null,
    @SerialName("ribbon_label") val ribbonLabel: String? = null,
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
)

@Serializable
data class BrewTrailerDto(
    @SerialName("vod_asset_id") val vodAssetId: Int? = null,
    @SerialName("is_drm") val isDrm: Boolean? = null,
    @SerialName("is_public") val isPublic: Boolean? = null,
    @SerialName("trailer_original_url") val trailerOriginalUrl: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
)
