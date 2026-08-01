package com.google.jetstream.data.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    /** Fetch the home catalog + hero images so the first tab paints quickly. */
    suspend fun warmHome() = withContext(Dispatchers.IO) {
        runCatching { movieRepository.prefetchHomePage(BrewPages.HOME) }
        movieRepository.peekHomeSections(BrewPages.HOME)?.let { prefetchImages(it) }
    }

    /** Fetch all catalog pages + enqueue hero/card images (background / rail prefetch). */
    suspend fun warmAll() = withContext(Dispatchers.IO) {
        coroutineScope {
            CatalogPages.map { page ->
                async { runCatching { movieRepository.prefetchHomePage(page) } }
            }.awaitAll()
        }
        CatalogPages.forEach { page ->
            movieRepository.peekHomeSections(page)?.let { prefetchImages(it) }
        }
    }

    private suspend fun prefetchImages(sections: List<HomeSection>) {
        sections.firstOrNull { it.type == HomeSectionType.Showcase }?.movies
            ?.take(4)
            ?.forEach { movie ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(BrewImageUrl.forShowcase(movie.posterUri))
                        .size(BrewImageUrl.SHOWCASE_WIDTH, BrewImageUrl.SHOWCASE_HEIGHT)
                        .build(),
                )
            }
        delay(2500)
        sections
            .asSequence()
            .filter {
                it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive
            }
            .take(4)
            .flatMap { it.movies.asSequence().take(8) }
            .forEach { movie ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(BrewImageUrl.forCard(movie.posterUri))
                        .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
                        .build(),
                )
            }
    }
}
