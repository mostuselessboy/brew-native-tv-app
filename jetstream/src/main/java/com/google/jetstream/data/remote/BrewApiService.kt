package com.google.jetstream.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BrewApiService {
    @GET("api/v1/vod/home-sections")
    suspend fun getHomeSections(
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
        @Query("page") page: String = BrewPages.HOME,
    ): List<BrewHomeSectionDto>

    @GET("api/v1/vod/get-campaign/{slug}")
    suspend fun getCampaign(
        @Path("slug") slug: String,
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
    ): BrewCampaignResponse

    @GET("api/v1/vod/also-watched")
    suspend fun getAlsoWatched(
        @Query("cv_name") cvName: String,
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
    ): BrewAlsoWatchedResponse

    @GET("api/v1/vod/related-movies")
    suspend fun getRelatedMovies(
        @Query("cv_name") cvName: String,
        @Query("campaign_version_id") campaignVersionId: Int? = null,
        @Query("locale") locale: String = BrewPages.DEFAULT_COUNTRY,
        @Query("lang") lang: String = "en",
    ): BrewRelatedMoviesResponse

    @GET("api/v1/vod/search")
    suspend fun searchProjects(
        @Query("query") query: String,
        @Query("limit") limit: Int = 48,
    ): BrewSearchResponse

    @POST("api/v1/auth/qr-code/generate")
    suspend fun generateQrCode(): BrewQrGenerateResponse

    @GET("api/v1/auth/qr-code/poll/{code}")
    suspend fun pollQrCode(@Path("code") code: String): BrewQrPollResponse

    @POST("api/auth/signinup/code")
    suspend fun sendAuthCode(@Body body: BrewAuthCodeRequest): BrewAuthCodeResponse

    @POST("api/auth/signinup/code/consume")
    suspend fun consumeAuthCode(@Body body: BrewAuthConsumeRequest): BrewAuthConsumeResponse

    @GET("api/v1/user/details")
    suspend fun getUserDetails(): BrewUserDetailsResponse

    @GET("api/v1/vod/continue-watching")
    suspend fun getContinueWatching(
        @Query("user_id") userId: String,
    ): BrewContinueWatchingResponse

    @GET("api/v1/vod/user-active-orders/{userId}")
    suspend fun getUserActiveOrders(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): BrewUserActiveOrdersResponse
}
