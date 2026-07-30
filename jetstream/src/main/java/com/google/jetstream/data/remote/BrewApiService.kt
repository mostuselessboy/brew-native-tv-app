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

    @GET("api/v1/vod/get-campaign/{slug}")
    suspend fun getCampaign(@Path("slug") slug: String): BrewCampaignResponse

    /** Customers also watched — same as vod-frontend `getRelatedMovies`. */
    @GET("api/v1/vod/also-watched")
    suspend fun getAlsoWatched(
        @Query("cv_name") cvName: String,
        @Query("country") country: String = BrewPages.DEFAULT_COUNTRY,
    ): BrewAlsoWatchedResponse
}
