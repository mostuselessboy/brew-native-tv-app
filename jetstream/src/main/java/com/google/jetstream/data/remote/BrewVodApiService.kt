package com.google.jetstream.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Authenticated VOD playback endpoints — mobile / tv-app parity. */
interface BrewVodApiService {

    @GET("api/v1/vod/check-purchase")
    suspend fun checkPurchase(
        @Query("user_id") userId: Int,
        @Query("cv_name") cvName: String? = null,
        @Query("campaign_version_id") campaignVersionId: Int? = null,
    ): BrewWrappedResponse<BrewCheckPurchaseResponse>

    @POST("api/v1/vod/start-playback")
    suspend fun startPlayback(
        @Body body: BrewStartPlaybackRequest,
    ): BrewWrappedResponse<BrewCheckPurchaseDetails?>

    @GET("api/v1/vod/asset")
    suspend fun getVodAsset(
        @Query("user_id") userId: Int,
        @Query("vod_asset_id") vodAssetId: Int? = null,
        @Query("cv_name") cvName: String? = null,
        @Query("campaign_version_id") campaignVersionId: Int? = null,
    ): BrewWrappedResponse<BrewVodAssetData>

    @GET("api/v1/vod/get-endscreen-recommendations")
    suspend fun getEndscreenRecommendations(
        @Query("campaign_id") campaignId: Int,
        @Query("project_type") projectType: String,
        @Query("user_id") userId: Int? = null,
        @Query("country") country: String = "in",
    ): BrewWrappedResponse<BrewEndscreenRecommendationsResponse>

    @GET("api/v1/vod/subscription-plans")
    suspend fun getSubscriptionPlans(
        @Query("country") country: String? = null,
        @Query("original_currency") originalCurrency: String? = null,
    ): BrewWrappedResponse<BrewSubscriptionPlansResponse>
}
