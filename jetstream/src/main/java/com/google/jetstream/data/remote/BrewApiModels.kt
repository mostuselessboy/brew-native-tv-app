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
    @SerialName("leave_date") val leaveDate: String? = null,
    @SerialName("monetization_model") val monetizationModel: List<String> = emptyList(),
    @SerialName("is_svod") val isSvod: Boolean? = null,
    @SerialName("is_tvod") val isTvod: Boolean? = null,
    @SerialName("is_store_content") val isStoreContent: Boolean? = null,
    @SerialName("available_for_buy") val availableForBuy: Boolean? = null,
    @SerialName("available_for_rent") val availableForRent: Boolean? = null,
    @SerialName("pricing_data") val pricingData: BrewPricingDataDto? = null,
)

@Serializable
data class BrewPricingDataDto(
    @SerialName("pricing_id") val pricingId: Int = 0,
    val buy: List<BrewPricingOptionDto> = emptyList(),
    val rent: List<BrewPricingOptionDto> = emptyList(),
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
    @SerialName("project_trailer") val projectTrailer: List<String> = emptyList(),
    @SerialName("cast_and_awards") val castAndAwards: BrewCastAndAwards? = null,
    val collections: List<BrewCollectionDto> = emptyList(),
    @SerialName("critic_reviews") val criticReviews: List<BrewCriticReviewDto> = emptyList(),
    @SerialName("user_reviews") val userReviews: List<BrewUserReviewDto> = emptyList(),
    @SerialName("vod_primary_language") val vodPrimaryLanguage: BrewLanguageDto? = null,
    val campaign: BrewCampaignMeta? = null,
    val project: BrewProjectDto? = null,
    val trailer: BrewTrailerDto? = null,
    @SerialName("purchase_cta") val purchaseCta: BrewPurchaseCtaDto? = null,
    @SerialName("pricing_data") val pricingData: BrewPricingDataDto? = null,
    @SerialName("monetization_model") val monetizationModels: List<String> = emptyList(),
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
    @SerialName("cv_name") val cvName: String? = null,
    val status: String? = null,
    @SerialName("trailer_original_url") val trailerOriginalUrl: String? = null,
    val appearance: BrewAppearanceDto? = null,
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
data class BrewAlsoWatchedMovieDto(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
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
    @SerialName("is_continue_watching") val isContinueWatching: Boolean? = null,
    @SerialName("percentage_watched") val percentageWatched: Float? = null,
)

@Serializable
data class BrewPurchaseCtaAddOnsDto(
    val coupons: Boolean = false,
    @SerialName("more_purchase_options") val morePurchaseOptions: Boolean = false,
)

@Serializable
data class BrewRelatedMoviesResponse(
    val success: Boolean = false,
    val data: BrewRelatedMoviesData? = null,
)

@Serializable
data class BrewRelatedMoviesData(
    @SerialName("related_movies") val relatedMovies: List<BrewRelatedMovieDetailDto> = emptyList(),
)

@Serializable
data class BrewRelatedMovieDetailDto(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("release_year") val releaseYear: Int? = null,
    @SerialName("vod_tag") val vodTag: String? = null,
    val appearance: BrewRelatedAppearanceDto? = null,
    @SerialName("monetization_model") val monetizationModels: List<String> = emptyList(),
    @SerialName("is_svod") val isSvod: Boolean? = null,
    @SerialName("is_tvod") val isTvod: Boolean? = null,
    @SerialName("is_store_content") val isStoreContent: Boolean? = null,
)

@Serializable
data class BrewRelatedAppearanceDto(
    val poster: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("background_art") val backgroundArt: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("vertical_background_art") val verticalBackgroundArt: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("horizontal_thumbnails") val horizontalThumbnails: kotlinx.serialization.json.JsonElement? = null,
)

@Serializable
data class BrewQrGenerateResponse(
    val success: Boolean = false,
    val code: String? = null,
    @SerialName("expiresAt") val expiresAt: Long? = null,
    val message: String? = null,
)

@Serializable
data class BrewQrPollResponse(
    val success: Boolean = false,
    val verified: Boolean = false,
    val email: String? = null,
    val phone: String? = null,
    val message: String? = null,
)

@Serializable
data class BrewAuthCodeRequest(
    @SerialName("isRNVerified") val isRNVerified: Boolean = true,
    val email: String? = null,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
)

@Serializable
data class BrewAuthCodeResponse(
    val status: String? = null,
    @SerialName("deviceId") val deviceId: String? = null,
    @SerialName("preAuthSessionId") val preAuthSessionId: String? = null,
)

@Serializable
data class BrewAuthConsumeRequest(
    @SerialName("userInputCode") val userInputCode: String,
    @SerialName("deviceId") val deviceId: String,
    @SerialName("preAuthSessionId") val preAuthSessionId: String,
)

@Serializable
data class BrewAuthConsumeResponse(
    val status: String? = null,
    val message: String? = null,
    val user: BrewAuthUserDto? = null,
)

@Serializable
data class BrewAuthUserDto(
    val id: Int? = null,
    val email: String? = null,
    val phone: String? = null,
    val name: String? = null,
    val username: String? = null,
    val picture: String? = null,
)

@Serializable
data class BrewUserDetailsResponse(
    val success: Boolean = false,
    val user: BrewUserDto? = null,
)

@Serializable
data class BrewUserDto(
    val id: Int? = null,
    val email: String? = null,
    val phone: String? = null,
    val name: String? = null,
)

@Serializable
data class BrewTrailerDto(
    @SerialName("trailer_original_url") val trailerOriginalUrl: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
)

@Serializable
data class BrewSearchResponse(
    val success: Boolean = false,
    val data: BrewSearchData? = null,
)

@Serializable
data class BrewSearchData(
    val results: List<BrewSearchProjectDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int? = null,
)

@Serializable
data class BrewSearchProjectDto(
    @SerialName("project_id") val projectId: Int? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster") val projectPoster: String? = null,
    @SerialName("release_year") val releaseYear: Int? = null,
    @SerialName("project_type") val projectType: String? = null,
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
)

@Serializable
data class BrewContinueWatchingResponse(
    val success: Boolean = false,
    val data: List<BrewContinueWatchingEntryDto> = emptyList(),
)

@Serializable
data class BrewContinueWatchingEntryDto(
    val id: Int? = null,
    @SerialName("content_data") val contentData: BrewContinueWatchingContentDto? = null,
)

@Serializable
data class BrewContinueWatchingContentDto(
    val id: Int? = null,
    @SerialName("campaign_version_id") val campaignVersionId: Int? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("project_poster_url") val projectPosterUrl: String? = null,
    @SerialName("background_art_url") val backgroundArtUrl: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("project_synopsis") val projectSynopsis: String? = null,
    val slug: String? = null,
    val country: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val progress: Float? = null,
    @SerialName("vod_tag") val vodTag: String? = null,
)

@Serializable
data class BrewUserActiveOrdersResponse(
    val success: Boolean = false,
    val data: BrewUserActiveOrdersData? = null,
    val orders: List<BrewLibraryOrderDto>? = null,
)

@Serializable
data class BrewUserActiveOrdersData(
    val orders: List<BrewLibraryOrderDto> = emptyList(),
)

@Serializable
data class BrewLibraryOrderDto(
    val id: Int? = null,
    @SerialName("purchase_type") val purchaseType: String? = null,
    @SerialName("campaign_version_id") val campaignVersionId: Int? = null,
    @SerialName("watch_expires_at") val watchExpiresAt: String? = null,
    @SerialName("order_expires_at") val orderExpiresAt: String? = null,
    @SerialName("campaign_info") val campaignInfo: BrewLibraryCampaignInfoDto? = null,
    @SerialName("rental_status") val rentalStatus: BrewLibraryRentalStatusDto? = null,
)

@Serializable
data class BrewLibraryCampaignInfoDto(
    @SerialName("campaign_id") val campaignId: Int? = null,
    @SerialName("cv_name") val cvName: String? = null,
    @SerialName("project_title") val projectTitle: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("project_release_date") val projectReleaseDate: String? = null,
    val country: String? = null,
    val appearance: BrewLibraryAppearanceDto? = null,
)

@Serializable
data class BrewLibraryAppearanceDto(
    @SerialName("background_art") val backgroundArt: BrewLibraryArtUrlDto? = null,
)

@Serializable
data class BrewLibraryArtUrlDto(
    val url: String? = null,
)

@Serializable
data class BrewLibraryRentalStatusDto(
    @SerialName("is_expired") val isExpired: Boolean? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)
