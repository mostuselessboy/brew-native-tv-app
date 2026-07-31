package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.LibraryShelfId
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MyLibraryPage
import com.google.jetstream.data.remote.BrewLibraryApiService
import com.google.jetstream.data.remote.BrewAddBookmarkRequest
import com.google.jetstream.data.remote.BrewMyLibraryRequest
import com.google.jetstream.data.remote.BrewMyLibraryShelfRequest
import com.google.jetstream.data.remote.LibraryMappers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val libraryApi: BrewLibraryApiService,
    private val json: Json,
) : LibraryRepository {

    @Volatile
    private var cachedPage: MyLibraryPage? = null

    @Volatile
    private var cachedUserId: Int? = null

    override suspend fun fetchMyLibrary(userId: Int): Result<MyLibraryPage> = runCatching {
        val response = libraryApi.getMyLibrary(
            userId = userId,
            body = BrewMyLibraryRequest(shelves = defaultLibraryShelves()),
        )
        val data = response.data ?: throw IllegalStateException(response.message ?: "Library unavailable")
        val page = LibraryMappers.parseMyLibrary(data, json)
        cachedPage = page
        cachedUserId = userId
        page
    }

    override suspend fun fetchContinueWatchingMovies(userId: Int): List<Movie> = runCatching {
        val response = libraryApi.getMyLibrary(
            userId = userId,
            body = BrewMyLibraryRequest(
                shelves = listOf(
                    BrewMyLibraryShelfRequest(
                        id = LibraryShelfId.ContinueWatching.apiId,
                        limit = CONTINUE_WATCHING_LIMIT,
                        offset = 0,
                    ),
                ),
            ),
        )
        val row = response.data?.rows?.firstOrNull {
            it.id == LibraryShelfId.ContinueWatching.apiId
        } ?: return emptyList()
        LibraryMappers.parseRowItems(row, LibraryShelfId.ContinueWatching, json)
            .map { item ->
                item.movie.copy(
                    watchProgressPercent = item.progressPercent.takeIf { it > 0 },
                    description = item.episodeLabel ?: item.movie.description,
                    vodAssetId = item.vodAssetId,
                    initialTimeSeconds = item.initialTimeSeconds.takeIf { it > 0 },
                    libraryClickAction = item.clickAction,
                )
            }
    }.getOrDefault(emptyList())

    override suspend fun prefetchMyLibrary(userId: Int) {
        if (cachedUserId == userId && cachedPage != null) return
        fetchMyLibrary(userId)
    }

    override fun peekMyLibrary(userId: Int): MyLibraryPage? {
        if (cachedUserId == userId) return cachedPage
        return null
    }

    override suspend fun loadMoreShelf(
        userId: Int,
        shelfId: String,
        offset: Int,
    ): Result<List<LibraryItem>> = runCatching {
        val response = libraryApi.getMyLibrary(
            userId = userId,
            body = BrewMyLibraryRequest(
                shelves = listOf(
                    BrewMyLibraryShelfRequest(
                        id = shelfId,
                        limit = SHELF_PAGE_SIZE,
                        offset = offset,
                    ),
                ),
            ),
        )
        val row = response.data?.rows?.firstOrNull { it.id == shelfId }
            ?: throw IllegalStateException(response.message ?: "Shelf unavailable")
        val shelfEnum = LibraryShelfId.fromApiId(shelfId)
            ?: throw IllegalArgumentException("Unknown shelf: $shelfId")
        LibraryMappers.parseRowItems(row, shelfEnum, json)
    }

    override suspend fun getBookmarkStatus(userId: Int, vodAssetId: Int): Boolean {
        if (vodAssetId <= 0) return false
        return runCatching {
            libraryApi.getBookmarkStatus(userId = userId, vodAssetId = vodAssetId)
                .data?.isBookmarked == true
        }.getOrDefault(false)
    }

    override suspend fun addBookmark(userId: Int, vodAssetId: Int): Result<Unit> = runCatching {
        libraryApi.addBookmark(BrewAddBookmarkRequest(userId = userId, vodAssetId = vodAssetId))
        Unit
    }

    override suspend fun removeBookmark(userId: Int, vodAssetId: Int): Result<Unit> = runCatching {
        libraryApi.removeBookmark(userId = userId, vodAssetId = vodAssetId)
        Unit
    }

    private companion object {
        const val SHELF_PAGE_SIZE = 12
        const val CONTINUE_WATCHING_LIMIT = 12

        fun defaultLibraryShelves(): List<BrewMyLibraryShelfRequest> = listOf(
            BrewMyLibraryShelfRequest(id = LibraryShelfId.ContinueWatching.apiId),
            BrewMyLibraryShelfRequest(id = LibraryShelfId.RateShare.apiId),
            BrewMyLibraryShelfRequest(id = LibraryShelfId.Purchased.apiId),
            BrewMyLibraryShelfRequest(id = LibraryShelfId.ExpiredPurchases.apiId),
            BrewMyLibraryShelfRequest(id = LibraryShelfId.Rented.apiId),
            BrewMyLibraryShelfRequest(id = LibraryShelfId.Bookmarks.apiId),
            BrewMyLibraryShelfRequest(id = LibraryShelfId.RentedExpired.apiId),
        )
    }
}
