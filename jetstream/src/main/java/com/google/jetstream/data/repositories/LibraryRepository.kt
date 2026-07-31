package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.MyLibraryPage

interface LibraryRepository {
    suspend fun fetchMyLibrary(userId: Int): Result<MyLibraryPage>
    suspend fun prefetchMyLibrary(userId: Int)
    fun peekMyLibrary(userId: Int): MyLibraryPage?
    suspend fun fetchContinueWatchingMovies(userId: Int): List<com.google.jetstream.data.entities.Movie>
    suspend fun loadMoreShelf(userId: Int, shelfId: String, offset: Int): Result<List<com.google.jetstream.data.entities.LibraryItem>>
    suspend fun getBookmarkStatus(userId: Int, vodAssetId: Int): Boolean
    suspend fun addBookmark(userId: Int, vodAssetId: Int): Result<Unit>
    suspend fun removeBookmark(userId: Int, vodAssetId: Int): Result<Unit>
}
