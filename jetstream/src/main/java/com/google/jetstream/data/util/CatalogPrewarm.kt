package com.google.jetstream.data.util

import android.content.Context
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private val CatalogPages = listOf(
    BrewPages.HOME,
    BrewPages.BREW_PLUS,
    BrewPages.SHORTS,
    BrewPages.STORE,
)

@Singleton
class CatalogPrewarm @Inject constructor(
    private val movieRepository: MovieRepository,
    @ApplicationContext private val context: Context,
) {
    /** Fetch home catalog + tiered image warmup for fast first paint. */
    suspend fun warmHome() = withContext(Dispatchers.IO) {
        runCatching { movieRepository.prefetchHomePage(BrewPages.HOME) }
        val sections = movieRepository.peekHomeSections(BrewPages.HOME)
        if (sections != null) {
            CatalogImagePrefetch.warmPage(context, BrewPages.HOME, sections)
        }
    }

    /** Fetch all catalog pages; images are tiered per page (critical then deferred). */
    suspend fun warmAll() = withContext(Dispatchers.IO) {
        coroutineScope {
            CatalogPages.map { page ->
                async { runCatching { movieRepository.prefetchHomePage(page) } }
            }.awaitAll()
        }
        CatalogPages.forEach { page ->
            val sections = movieRepository.peekHomeSections(page)
            if (sections != null) {
                CatalogImagePrefetch.warmPage(context, page, sections)
            }
        }
    }
}
