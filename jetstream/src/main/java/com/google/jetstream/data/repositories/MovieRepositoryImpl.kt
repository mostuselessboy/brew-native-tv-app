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
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieCategory
import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.entities.MovieCategoryList
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.remote.BrewAlsoWatchedMovieDto
import com.google.jetstream.data.remote.BrewMappers.toReviewSummary
import com.google.jetstream.data.remote.BrewMappers.toUserReview
import com.google.jetstream.data.remote.BrewApiService
import com.google.jetstream.data.remote.BrewContentDataDto
import com.google.jetstream.data.remote.BrewHomeSectionDto
import com.google.jetstream.data.remote.BrewMappers.toCollectionSectionDetails
import com.google.jetstream.data.remote.BrewMappers.toHomeSection
import com.google.jetstream.data.remote.BrewMappers.toMovie
import com.google.jetstream.data.remote.BrewMappers.toMovieDetails
import com.google.jetstream.data.remote.BrewMappers.genresToCategories
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.remote.BrewCampaignData
import com.google.jetstream.data.util.BrewArtworkUrls
import com.google.jetstream.data.util.CardCommerce
import com.google.jetstream.data.util.DailyRotatingArtwork
import com.google.jetstream.data.util.VodTagBadge
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val brewApiService: BrewApiService,
) : MovieRepository {

    private val mutex = Mutex()
    private val cachedSectionsByPage = mutableMapOf<String, List<BrewHomeSectionDto>>()
    private val cachedMappedSectionsByPage = mutableMapOf<String, List<HomeSection>>()
    private var campaignIdToSlug: Map<Int, String> = emptyMap()
    private val slugAliases = mutableMapOf<String, String>()

    private companion object {
        val CatalogPages = listOf(
            BrewPages.HOME,
            BrewPages.BREW_PLUS,
            BrewPages.SHORTS,
            BrewPages.STORE,
        )
    }

    private fun registerSlugAliases(data: BrewContentDataDto, cid: Int?) {
        val slug = data.slug?.takeIf { it.isNotBlank() } ?: return
        fun put(alias: String?) {
            alias?.trim()?.takeIf { it.isNotBlank() }?.let { slugAliases[it.lowercase()] = slug }
        }
        put(slug)
        data.id?.let { slugAliases[it.toString()] = slug }
        cid?.let { slugAliases[it.toString()] = slug }
        put(data.localizedCvName)
        put(data.canonicalCvName)
        data.projectId?.let { slugAliases[it.toString()] = slug }
    }

    private fun registerCampaignAliases(data: BrewCampaignData) {
        val slug = data.preferredSlug?.takeIf { it.isNotBlank() }
            ?: data.campaign?.cvName?.takeIf { it.isNotBlank() }
            ?: return
        fun put(alias: String?) {
            alias?.trim()?.takeIf { it.isNotBlank() }?.let { slugAliases[it.lowercase()] = slug }
        }
        put(slug)
        data.campaign?.id?.let { slugAliases[it.toString()] = slug }
        data.campaign?.cvName?.let { put(it) }
        data.project?.id?.let { slugAliases[it.toString()] = slug }
    }

    private suspend fun warmSlugIndex() {
        CatalogPages.forEach { page ->
            runCatching { homeSections(page) }
        }
    }

    private fun BrewAlsoWatchedMovieDto.toAlsoWatchedMovie(): Movie? {
        val id = cvName?.takeIf { it.isNotBlank() } ?: return null
        val name = projectTitle?.takeIf { it.isNotBlank() } ?: return null
        val backgroundArt = BrewArtworkUrls.asUrl(appearance?.get("background_art"))
        val horizontalThumbnails = BrewArtworkUrls.horizontalAlternatesFromAppearance(appearance)
        val posterFallback = projectPoster?.takeIf { it.isNotBlank() }
            ?: BrewArtworkUrls.asUrl(appearance?.get("poster"))
        val landscape = DailyRotatingArtwork.pickDailyLandscape(
            backgroundArtUrl = backgroundArt,
            horizontalThumbnails = horizontalThumbnails,
            fallback = posterFallback ?: BrewArtworkUrls.landscapeFromAppearance(appearance),
        ).takeIf { it.isNotBlank() }
            ?: posterFallback?.takeIf { it.isNotBlank() }
            ?: return null
        val chrome = CardCommerce.resolve(
            monetizationModel = monetizationModel,
            pricingData = null,
            isSvod = isSvod,
            isTvod = isTvod,
            availableForBuy = availableForBuy,
            availableForRent = availableForRent,
            isStoreContent = isStoreContent,
        )
        return Movie(
            id = id,
            videoUri = "",
            subtitleUri = null,
            posterUri = landscape,
            name = name,
            description = shortDescription.orEmpty(),
            vodTagLabel = VodTagBadge.movieCardLabel(vodTag),
            isFestivalTag = VodTagBadge.isFestivalStyle(vodTag),
            showStore = chrome.showStore,
            showBrewPlus = chrome.showPlus,
        )
    }

    private suspend fun homeSections(page: String = BrewPages.HOME): List<BrewHomeSectionDto> {
        cachedSectionsByPage[page]?.let { return it }
        return mutex.withLock {
            cachedSectionsByPage[page]?.let { return it }
            val sections = brewApiService.getHomeSections(
                country = BrewPages.DEFAULT_COUNTRY,
                page = page,
            )
            cachedSectionsByPage[page] = sections
            val newIdMap = mutableMapOf<Int, String>()
            sections.forEach { section ->
                section.content.forEach { item ->
                    val data = item.contentData ?: return@forEach
                    registerSlugAliases(data, item.cid)
                    val id = data.id ?: item.cid ?: return@forEach
                    val slug = data.slug?.takeIf { it.isNotBlank() } ?: return@forEach
                    newIdMap[id] = slug
                }
            }
            campaignIdToSlug = campaignIdToSlug + newIdMap
            sections
        }
    }

    private suspend fun allContent(page: String = BrewPages.HOME): List<BrewContentDataDto> {
        return homeSections(page)
            .flatMap { it.content }
            .mapNotNull { it.contentData }
            .distinctBy { it.slug ?: it.id?.toString() }
    }

    private suspend fun allMovies(
        thumbnailType: ThumbnailType = ThumbnailType.Standard,
        page: String = BrewPages.HOME,
    ): MovieList {
        return allContent(page).mapNotNull { it.toMovie(thumbnailType) }
    }

    private suspend fun mappedHomeSections(page: String = BrewPages.HOME): List<HomeSection> {
        cachedMappedSectionsByPage[page]?.let { return it }
        val mapped = homeSections(page)
            .mapNotNull { it.toHomeSection() }
            .filter { it.type != HomeSectionType.RandomMoviePicker }
            .map { section ->
                section.copy(
                    movies = when (section.type) {
                        HomeSectionType.Showcase -> section.movies
                        HomeSectionType.Immersive -> section.movies.take(8)
                        HomeSectionType.Row -> section.movies.take(8)
                        HomeSectionType.RandomMoviePicker -> section.movies
                    }
                )
            }
        cachedMappedSectionsByPage[page] = mapped
        return mapped
    }

    override fun peekHomeSections(page: String): List<HomeSection>? =
        cachedMappedSectionsByPage[page]

    private fun resolveSlug(movieId: String): String {
        val trimmed = movieId.trim()
        trimmed.toIntOrNull()?.let { campaignId ->
            campaignIdToSlug[campaignId]?.let { return it }
            slugAliases[campaignId.toString()]?.let { return it }
        }
        slugAliases[trimmed.lowercase()]?.let { return it }
        return trimmed
    }

    private suspend fun fetchCampaignData(requestedSlug: String): BrewCampaignData {
        val resolved = resolveSlug(requestedSlug)
        val candidates = buildList {
            add(resolved)
            if (resolved != requestedSlug.trim()) add(requestedSlug.trim())
            slugAliases[resolved.lowercase()]?.let { add(it) }
        }.distinct().filter { it.isNotBlank() }

        for (candidate in candidates) {
            val response = runCatching { brewApiService.getCampaign(candidate) }.getOrNull()
            val data = response?.data
            if (data != null) {
                registerCampaignAliases(data)
                return data
            }
        }
        error("Campaign not found for $requestedSlug")
    }

    override fun getHomeSections(page: String): Flow<List<HomeSection>> = flow {
        emit(mappedHomeSections(page))
    }

    override fun getFeaturedMovies(): Flow<MovieList> = flow {
        val showcase = mappedHomeSections().firstOrNull { it.type == HomeSectionType.Showcase }
        emit(showcase?.movies ?: allMovies(ThumbnailType.Long).take(8))
    }

    override fun getTrendingMovies(): Flow<MovieList> = flow {
        val rows = mappedHomeSections().filter { it.type == HomeSectionType.Row }
        emit(rows.getOrNull(0)?.movies ?: allMovies().take(10))
    }

    override fun getTop10Movies(): Flow<MovieList> = flow {
        val immersive = mappedHomeSections().firstOrNull { it.type == HomeSectionType.Immersive }
        emit(immersive?.movies?.take(10) ?: allMovies(ThumbnailType.Long).take(10))
    }

    override fun getNowPlayingMovies(): Flow<MovieList> = flow {
        val rows = mappedHomeSections().filter { it.type == HomeSectionType.Row }
        emit(rows.getOrNull(1)?.movies ?: allMovies().drop(10).take(10))
    }

    override fun getMovieCategories(): Flow<MovieCategoryList> = flow {
        emit(genresToCategories(allContent()))
    }

    override suspend fun getMovieCategoryDetails(categoryId: String): MovieCategoryDetails {
        val categories = genresToCategories(allContent())
        val category = categories.find { it.id == categoryId } ?: categories.firstOrNull()
            ?: MovieCategory(id = categoryId, name = "Movies")
        val movies = allContent()
            .filter { content ->
                content.genres.any { genre ->
                    category.name.equals(genre, ignoreCase = true) ||
                        categoryId.contains(genre, ignoreCase = true)
                }
            }
            .mapNotNull { it.toMovie() }
        return MovieCategoryDetails(
            id = category.id,
            name = category.name,
            movies = movies.ifEmpty { allMovies().take(20) },
        )
    }

    override suspend fun warmHomeCache() {
        runCatching { homeSections(BrewPages.HOME) }
    }

    override suspend fun prefetchHomePage(page: String) {
        runCatching { mappedHomeSections(page) }
    }

    private suspend fun findCatalogContent(slug: String): BrewContentDataDto? {
        CatalogPages.forEach { page ->
            allContent(page).firstOrNull { item ->
                item.slug?.equals(slug, ignoreCase = true) == true
            }?.let { return it }
        }
        return null
    }

    override suspend fun getMovieDetails(movieId: String): MovieDetails {
        warmSlugIndex()
        val slug = resolveSlug(movieId.trim())
        require(slug.isNotBlank()) { "Blank movie id" }
        val data = fetchCampaignData(slug)
        val catalogOverlay = findCatalogContent(slug)

        val cvName = data.campaign?.cvName?.takeIf { it.isNotBlank() }
            ?: data.preferredSlug?.takeIf { it.isNotBlank() }
            ?: slug

        val campaignVersionId = data.campaign?.campaignVersionId
        val campaignId = data.campaign?.id?.toString()?.takeIf { it.isNotBlank() } ?: slug

        val customersAlsoWatched = runCatching {
            brewApiService.getAlsoWatched(
                cvName = cvName,
                country = BrewPages.DEFAULT_COUNTRY,
            ).data?.relatedMovies.orEmpty()
        }.getOrDefault(emptyList())
            .mapNotNull { it.toAlsoWatchedMovie() }
            .filter { it.id != slug }
            .distinctBy { it.id }
            .take(12)

        val alsoWatchedIds = customersAlsoWatched.map { it.id }.toSet()

        val relatedMovies = if (campaignVersionId != null && campaignVersionId > 0) {
            runCatching {
                brewApiService.getRelatedMovies(campaignVersionId = campaignVersionId)
                    .data?.relatedMovies.orEmpty()
                    .mapNotNull { it.toAlsoWatchedMovie() }
                    .filter { it.id != slug && it.id !in alsoWatchedIds }
                    .distinctBy { it.id }
                    .take(12)
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val userReviewComments = runCatching {
            brewApiService.getComments(
                campaignId = campaignId,
                limit = 20,
                country = BrewPages.DEFAULT_COUNTRY,
            ).data
        }.getOrNull()

        val reviewCards = userReviewComments?.comments.orEmpty()
            .mapNotNull { it.toUserReview() }
        val reviewSummary = userReviewComments?.toReviewSummary()
        val userCountry = data.userCountry?.takeIf { it.isNotBlank() }
            ?: BrewPages.DEFAULT_COUNTRY

        return data.toMovieDetails(
            requestedId = slug,
            customersAlsoWatched = customersAlsoWatched,
            relatedMovies = relatedMovies,
            userReviewComments = reviewCards,
            reviewSummary = reviewSummary,
            userCountry = userCountry,
            catalogOverlay = catalogOverlay,
        )
    }

    override suspend fun searchMovies(query: String): MovieList {
        if (query.isBlank()) return emptyList()
        return allMovies().filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }
    }

    override fun getMoviesWithLongThumbnail(): Flow<MovieList> = flow {
        emit(allMovies(ThumbnailType.Long))
    }

    override fun getMovies(): Flow<MovieList> = flow {
        emit(allMovies())
    }

    override fun getPopularFilmsThisWeek(): Flow<MovieList> = flow {
        val rows = mappedHomeSections().filter { it.type == HomeSectionType.Row }
        emit(rows.getOrNull(2)?.movies ?: allMovies().drop(5).take(10))
    }

    override fun getTVShows(): Flow<MovieList> = flow {
        // Brew home feed is movie-focused; surface a curated tray as "shows" for now.
        val rows = mappedHomeSections().filter { it.type == HomeSectionType.Row }
        emit(rows.getOrNull(3)?.movies ?: allMovies(ThumbnailType.Long).take(8))
    }

    override fun getBingeWatchDramas(): Flow<MovieList> = flow {
        val drama = allContent()
            .filter { content ->
                content.genres.any { it.contains("Drama", ignoreCase = true) }
            }
            .mapNotNull { it.toMovie() }
        emit(drama.ifEmpty { allMovies().take(10) })
    }

    override fun getFavouriteMovies(): Flow<MovieList> = flow {
        emit(allMovies().take(28))
    }

    override suspend fun getCollectionSection(sectionId: String, page: Int): CollectionSectionDetails {
        val response = brewApiService.getHomeSectionById(
            sectionId = sectionId,
            page = page,
            pageSize = 50,
        )
        val section = response.data
            ?: throw IllegalStateException("Collection section not found: $sectionId")
        return section.toCollectionSectionDetails()
            ?: throw IllegalStateException("Collection section empty: $sectionId")
    }
}
