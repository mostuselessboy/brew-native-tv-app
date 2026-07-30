package com.google.jetstream.data.repositories

import android.net.Uri
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieCategory
import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.entities.MovieCategoryList
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.remote.BrewAlsoWatchedMovieDto
import com.google.jetstream.data.remote.BrewCampaignData
import com.google.jetstream.data.remote.BrewApiService
import com.google.jetstream.data.remote.BrewContentDataDto
import com.google.jetstream.data.remote.BrewHomeSectionDto
import com.google.jetstream.data.remote.BrewMappers.toHomeSection
import com.google.jetstream.data.remote.BrewMappers.toMovie
import com.google.jetstream.data.remote.BrewMappers.toMovieDetails
import com.google.jetstream.data.remote.BrewMappers.genresToCategories
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.remote.BrewRelatedMovieDetailDto
import com.google.jetstream.data.util.BrewArtworkUrls
import com.google.jetstream.data.util.CardCommerce
import com.google.jetstream.data.util.VodTagBadge
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
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
    private val campaignCache = mutableMapOf<String, BrewCampaignData>()
    private var campaignIdToSlug: Map<Int, String> = emptyMap()
    private var cvNameToSlug: Map<String, String> = emptyMap()

    private fun BrewAlsoWatchedMovieDto.toAlsoWatchedMovie(): Movie? {
        val id = cvName?.takeIf { it.isNotBlank() }
            ?: campaignId?.let { campaignIdToSlug[it] }
            ?: return null
        val name = projectTitle?.takeIf { it.isNotBlank() } ?: return null
        val backdrop = BrewArtworkUrls.landscapeFromAppearance(appearance)
        val poster = backdrop
            ?: projectPoster?.takeIf { it.isNotBlank() }
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
            posterUri = poster,
            backdropUri = backdrop ?: poster,
            name = name,
            description = shortDescription.orEmpty(),
            vodTagLabel = VodTagBadge.movieCardLabel(vodTag),
            isFestivalTag = VodTagBadge.isFestivalStyle(vodTag),
            showStore = chrome.showStore,
            showBrewPlus = chrome.showPlus,
        )
    }

    private fun BrewRelatedMovieDetailDto.toRelatedMovie(): Movie? {
        val id = cvName?.takeIf { it.isNotBlank() }
            ?: campaignId?.let { campaignIdToSlug[it] }
            ?: return null
        val name = projectTitle?.takeIf { it.isNotBlank() } ?: return null
        val backdrop = BrewArtworkUrls.landscapeFromRelated(appearance)
        val poster = backdrop
            ?: projectPoster?.takeIf { it.isNotBlank() }
            ?: return null
        val chrome = CardCommerce.resolve(
            monetizationModel = monetizationModels,
            pricingData = null,
            isSvod = isSvod,
            isTvod = isTvod,
            availableForBuy = null,
            availableForRent = null,
            isStoreContent = isStoreContent,
        )
        return Movie(
            id = id,
            videoUri = "",
            subtitleUri = null,
            posterUri = poster,
            backdropUri = backdrop ?: poster,
            name = name,
            description = "",
            year = releaseYear?.takeIf { it > 0 }?.toString(),
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
            campaignIdToSlug = campaignIdToSlug + sections
                .flatMap { it.content }
                .mapNotNull { item ->
                    val data = item.contentData ?: return@mapNotNull null
                    val id = data.id ?: item.cid ?: return@mapNotNull null
                    val slug = data.slug?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    id to slug
                }
                .toMap()
            cvNameToSlug = cvNameToSlug + sections
                .flatMap { it.content }
                .mapNotNull { item ->
                    val data = item.contentData ?: return@mapNotNull null
                    val slug = data.slug?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    slug to slug
                }
                .toMap()
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

    private suspend fun ensureSlugIndex() {
        listOf(
            BrewPages.HOME,
            BrewPages.BREW_PLUS,
            BrewPages.STORE,
            BrewPages.SHORTS,
        ).forEach { page ->
            runCatching { homeSections(page) }
        }
    }

    private fun findCachedMovie(movieId: String): Movie? {
        val key = movieId.trim()
        return cachedMappedSectionsByPage.values
            .flatten()
            .flatMap { it.movies }
            .firstOrNull {
                it.id == key ||
                    it.id.equals(key, ignoreCase = true) ||
                    it.id.replace('_', '-') == key.replace('_', '-')
            }
    }

    private fun Movie.toFallbackMovieDetails(): MovieDetails = MovieDetails(
        id = id,
        videoUri = videoUri,
        subtitleUri = subtitleUri,
        posterUri = posterUri,
        name = name,
        description = description.ifBlank { "—" },
        pgRating = "NR",
        releaseDate = listOfNotNull(year, country).filter { it.isNotBlank() }.joinToString(" • ")
            .ifBlank { "—" },
        categories = genres,
        duration = duration?.takeIf { it.isNotBlank() } ?: "—",
        director = "—",
        screenplay = "—",
        music = "—",
        castAndCrew = emptyList(),
        status = "Released",
        originalLanguage = "—",
        budget = "—",
        revenue = "—",
    )

    private suspend fun fetchCampaign(movieId: String): BrewCampaignData {
        val trimmed = movieId.trim()
        campaignCache[trimmed]?.let { return it }

        val candidates = buildList {
            add(trimmed)
            cvNameToSlug[trimmed]?.let { add(it) }
            trimmed.toIntOrNull()?.let { id -> campaignIdToSlug[id]?.let { add(it) } }
            if (trimmed.contains('_')) add(trimmed.replace('_', '-'))
            if (trimmed.contains('-')) add(trimmed.replace('-', '_'))
            Uri.decode(trimmed).takeIf { it != trimmed }?.let { add(it) }
        }.distinct().filter { it.isNotBlank() }

        suspend fun tryFetch(slug: String): BrewCampaignData? {
            campaignCache[slug]?.let { return it }
            repeat(2) { attempt ->
                if (attempt > 0) delay(300L)
                val response = runCatching {
                    brewApiService.getCampaign(
                        slug = slug,
                        country = BrewPages.DEFAULT_COUNTRY,
                    )
                }.getOrNull() ?: return@repeat
                val data = response.data?.takeIf { response.success || it.preferredSlug != null }
                    ?: return@repeat
                cacheCampaign(data, slug)
                return data
            }
            return null
        }

        for (candidate in candidates) {
            tryFetch(candidate)?.let { return it }
        }

        ensureSlugIndex()
        val retryCandidates = buildList {
            add(trimmed)
            cvNameToSlug[trimmed]?.let { add(it) }
            trimmed.toIntOrNull()?.let { id -> campaignIdToSlug[id]?.let { add(it) } }
            if (trimmed.contains('_')) add(trimmed.replace('_', '-'))
            if (trimmed.contains('-')) add(trimmed.replace('-', '_'))
        }.distinct().filter { it.isNotBlank() }

        for (candidate in retryCandidates) {
            tryFetch(candidate)?.let { return it }
        }

        error("Campaign not found for $movieId")
    }

    private fun cacheCampaign(data: BrewCampaignData, requestedKey: String) {
        val preferred = data.preferredSlug?.takeIf { it.isNotBlank() } ?: requestedKey
        campaignCache[requestedKey] = data
        campaignCache[preferred] = data
        data.campaign?.cvName?.takeIf { it.isNotBlank() }?.let { cv ->
            campaignCache[cv] = data
            cvNameToSlug = cvNameToSlug + (cv to preferred)
        }
        data.campaign?.id?.let { id ->
            campaignIdToSlug = campaignIdToSlug + (id to preferred)
        }
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

    override suspend fun getMovieDetails(movieId: String): MovieDetails {
        require(movieId.trim().isNotBlank()) { "Blank movie id" }
        return runCatching { loadMovieDetails(movieId) }
            .getOrElse { error ->
                findCachedMovie(movieId)?.toFallbackMovieDetails()
                    ?: throw error
            }
    }

    private suspend fun loadMovieDetails(movieId: String): MovieDetails {
        val data = fetchCampaign(movieId)
        val resolvedSlug = data.preferredSlug?.takeIf { it.isNotBlank() }
            ?: data.campaign?.cvName?.takeIf { it.isNotBlank() }
            ?: movieId.trim()
        val cvName = data.campaign?.cvName?.takeIf { it.isNotBlank() } ?: resolvedSlug
        val campaignVersionId = data.campaign?.id

        val alsoWatched = runCatching {
            brewApiService.getAlsoWatched(
                cvName = cvName,
                country = BrewPages.DEFAULT_COUNTRY,
            ).data?.relatedMovies.orEmpty()
        }.getOrDefault(emptyList())
            .mapNotNull { it.toAlsoWatchedMovie() }
            .filter { it.id != resolvedSlug && it.id != cvName }
            .distinctBy { it.id }
            .take(12)

        val relatedMovies = runCatching {
            brewApiService.getRelatedMovies(
                cvName = cvName,
                campaignVersionId = campaignVersionId,
            ).data?.relatedMovies.orEmpty()
        }.getOrDefault(emptyList())
            .mapNotNull { it.toRelatedMovie() }
            .filter { it.id != resolvedSlug && it.id != cvName }
            .distinctBy { it.id }
            .take(12)

        return runCatching {
            data.toMovieDetails(
                requestedId = resolvedSlug,
                alsoWatchedMovies = alsoWatched,
                relatedMovies = relatedMovies,
            )
        }.getOrElse {
            data.toMovieDetails(
                requestedId = resolvedSlug,
                alsoWatchedMovies = emptyList(),
                relatedMovies = emptyList(),
            )
        }
    }

    override suspend fun searchMovies(query: String): MovieList {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val response = runCatching {
            brewApiService.searchProjects(query = trimmed, limit = 48)
        }.getOrNull() ?: return fallbackLocalSearch(trimmed)

        val apiResults = response.data?.results.orEmpty()
            .mapNotNull { row ->
                val slug = row.cvName?.takeIf { it.isNotBlank() }
                    ?: row.campaignId?.let { campaignIdToSlug[it] }
                    ?: return@mapNotNull null
                val name = row.projectTitle?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val poster = row.projectPoster?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                row.campaignId?.let { id ->
                    campaignIdToSlug = campaignIdToSlug + (id to slug)
                }
                cvNameToSlug = cvNameToSlug + (slug to slug)
                Movie(
                    id = slug,
                    videoUri = "",
                    subtitleUri = null,
                    posterUri = poster,
                    name = name,
                    description = "",
                    year = row.releaseYear?.takeIf { it > 0 }?.toString(),
                )
            }
            .distinctBy { it.id }

        return apiResults.ifEmpty { fallbackLocalSearch(trimmed) }
    }

    private suspend fun fallbackLocalSearch(query: String): MovieList {
        runCatching { homeSections(BrewPages.HOME) }
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
}
