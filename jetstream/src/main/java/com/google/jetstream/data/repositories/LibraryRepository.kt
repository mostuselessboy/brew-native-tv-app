package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList

data class MyLibraryContent(
    val rented: MovieList = emptyList(),
    val purchased: MovieList = emptyList(),
    val expired: MovieList = emptyList(),
) {
    val isEmpty: Boolean get() = rented.isEmpty() && purchased.isEmpty() && expired.isEmpty()
}

interface LibraryRepository {
    suspend fun getContinueWatching(userId: Int): MovieList
    suspend fun getMyLibrary(userId: Int): MyLibraryContent
}
