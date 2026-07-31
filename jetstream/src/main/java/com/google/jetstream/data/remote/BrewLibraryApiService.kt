package com.google.jetstream.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Authenticated VOD library + bookmark endpoints — mobile-viewer parity. */
interface BrewLibraryApiService {

    @POST("api/v1/vod/my-library/{userId}")
    suspend fun getMyLibrary(
        @Path("userId") userId: Int,
        @Query("lang") lang: String = "en-in",
        @Body body: BrewMyLibraryRequest = BrewMyLibraryRequest(),
    ): BrewWrappedResponse<BrewMyLibraryResponse>

    @GET("api/v1/vod/bookmarks/status")
    suspend fun getBookmarkStatus(
        @Query("user_id") userId: Int,
        @Query("vod_asset_id") vodAssetId: Int,
    ): BrewWrappedResponse<BrewBookmarkStatusDto>

    @POST("api/v1/vod/bookmarks")
    suspend fun addBookmark(
        @Body body: BrewAddBookmarkRequest,
    ): BrewWrappedResponse<BrewBookmarkItemDto>

    @DELETE("api/v1/vod/bookmarks")
    suspend fun removeBookmark(
        @Query("user_id") userId: Int,
        @Query("vod_asset_id") vodAssetId: Int,
    ): BrewWrappedResponse<BrewBookmarkItemDto?>
}
