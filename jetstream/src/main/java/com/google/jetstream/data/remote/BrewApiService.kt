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

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BrewApiService {
    @GET("api/v1/vod/home-sections")
    suspend fun getHomeSections(
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
        @Query("page") page: String = BrewPages.HOME,
    ): List<BrewHomeSectionDto>

    /** Dice / surprise-me catalog for search suggestions. */
    @GET("api/v1/vod/dice-data")
    suspend fun getDiceData(
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
        @Query("lang") lang: String = "en",
    ): List<BrewHomeSectionDto>

    @GET("api/v1/vod/home-sections/{sectionId}")
    suspend fun getHomeSectionById(
        @Path("sectionId") sectionId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("locale") locale: String = "en-in",
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
    ): BrewHomeSectionResponse

    @GET("api/v1/vod/get-campaign/{slug}")
    suspend fun getCampaign(@Path("slug") slug: String): BrewCampaignResponse

    /** Customers also watched — vod `getRelatedMovies` → `/also-watched`. */
    @GET("api/v1/vod/also-watched")
    suspend fun getAlsoWatched(
        @Query("cv_name") cvName: String,
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
    ): BrewAlsoWatchedResponse

    /** Related movies at bottom — vod `getRelatedMoviesV1` → `/related-movies`. */
    @GET("api/v1/vod/related-movies")
    suspend fun getRelatedMovies(
        @Query("campaign_version_id") campaignVersionId: Int,
    ): BrewAlsoWatchedResponse

    /** User reviews — vod `GET /api/v1/comments/:campaign_id`. */
    @GET("api/v1/comments/{campaignId}")
    suspend fun getComments(
        @Path("campaignId") campaignId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5,
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
    ): BrewCommentsResponse

    /** Cast member details endpoint. */
    @GET("api/v1/vod/cast-member/{id}")
    suspend fun getCastMember(
        @Path("id") id: String,
        @Query("lang") lang: String = "en",
    ): BrewCastMemberProfileResponse
}

