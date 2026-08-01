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

package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.CollectionSectionDetails
import com.google.jetstream.data.entities.DiceSuggestions
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.entities.MovieCategoryList
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getHomeSections(page: String = "brew-home"): Flow<List<HomeSection>>
    fun getFeaturedMovies(): Flow<MovieList>
    fun getTrendingMovies(): Flow<MovieList>
    fun getTop10Movies(): Flow<MovieList>
    fun getNowPlayingMovies(): Flow<MovieList>
    fun getMovieCategories(): Flow<MovieCategoryList>
    suspend fun getMovieCategoryDetails(categoryId: String): MovieCategoryDetails
    suspend fun warmHomeCache()
    suspend fun prefetchHomePage(page: String)
    fun peekHomeSections(page: String): List<HomeSection>?
    /** Clears cached, potentially per-user-personalized home sections. Call on account/profile switch. */
    fun clearHomeCache()
    /** Best-effort lookup from warmed catalog caches (poster for detail skeleton, etc.). */
    fun peekMovieFromCatalog(movieId: String): Movie?
    suspend fun getMovieDetails(movieId: String): MovieDetails
    suspend fun getCollectionSection(sectionId: String, page: Int = 1): CollectionSectionDetails
    suspend fun searchMovies(query: String): MovieList
    suspend fun getDiceSuggestions(): DiceSuggestions
    fun getMoviesWithLongThumbnail(): Flow<MovieList>
    fun getMovies(): Flow<MovieList>
    fun getPopularFilmsThisWeek(): Flow<MovieList>
    fun getTVShows(): Flow<MovieList>
    fun getBingeWatchDramas(): Flow<MovieList>
    fun getFavouriteMovies(): Flow<MovieList>
    suspend fun getCastMember(id: String): com.google.jetstream.data.remote.BrewCastMemberDetailDto?
    suspend fun getShowcaseAccess(userId: Int): com.google.jetstream.data.remote.BrewShowcaseAccessResponse?
    suspend fun joinWaitlist(userId: Int, campaignVersionId: Int): Result<com.google.jetstream.data.remote.BrewJoinWaitlistResponse>
    suspend fun getCampaignSubtitles(movieSlug: String): List<com.google.jetstream.data.entities.PlaybackSubtitle>
}

