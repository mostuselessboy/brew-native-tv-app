package com.google.jetstream.data.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import kotlinx.coroutines.delay

/**
 * Tiered catalog image warmup — critical hero/first-row cards first, rest deferred so
 * cold start doesn't flood the network and block visible content.
 */
object CatalogImagePrefetch {

    private const val SHOWCASE_IMMEDIATE = 2
    private const val SHOWCASE_NEAR = 6
    private const val FIRST_ROW_CARDS = 5
    private const val NEAR_ROW_COUNT = 2
    private const val NEAR_ROW_CARDS = 8
    private const val BACKGROUND_ROW_LIMIT = 6
    private const val BACKGROUND_ROW_CARDS = 4
    private const val DEFERRED_PREFETCH_DELAY_MS = 2000L

    private val warmedPages = mutableSetOf<String>()
    private val criticalWarmedPages = mutableSetOf<String>()

    /** First paint: showcase slides + leading cards in the first tray. */
    fun enqueueCritical(context: Context, sections: List<HomeSection>) {
        sections.firstOrNull { it.type == HomeSectionType.Showcase }?.movies
            ?.take(SHOWCASE_IMMEDIATE)
            .orEmpty()
            .forEach { enqueueShowcase(context, it.posterUri) }

        traySections(sections)
            .take(1)
            .flatMap { it.movies.take(FIRST_ROW_CARDS) }
            .forEach { enqueueCard(context, it.posterUri) }
    }

    /** After UI is interactive — remaining showcase + nearby trays. */
    suspend fun enqueueDeferred(context: Context, sections: List<HomeSection>) {
        delay(DEFERRED_PREFETCH_DELAY_MS)
        enqueueNearAndBackground(context, sections)
    }

    /** Startup path: critical immediately, deferred once per catalog page. */
    suspend fun warmPage(context: Context, pageKey: String, sections: List<HomeSection>) {
        if (pageKey !in criticalWarmedPages) {
            criticalWarmedPages.add(pageKey)
            enqueueCritical(context, sections)
        }
        if (pageKey in warmedPages) return
        warmedPages.add(pageKey)
        enqueueDeferred(context, sections)
    }

    private fun enqueueNearAndBackground(context: Context, sections: List<HomeSection>) {
        sections.firstOrNull { it.type == HomeSectionType.Showcase }?.movies
            ?.drop(SHOWCASE_IMMEDIATE)
            ?.take(SHOWCASE_NEAR - SHOWCASE_IMMEDIATE)
            .orEmpty()
            .forEach { enqueueShowcase(context, it.posterUri) }

        traySections(sections)
            .take(NEAR_ROW_COUNT)
            .forEachIndexed { index, section ->
                val movies = if (index == 0) {
                    section.movies.drop(FIRST_ROW_CARDS).take(NEAR_ROW_CARDS)
                } else {
                    section.movies.take(NEAR_ROW_CARDS)
                }
                movies.forEach { enqueueCard(context, it.posterUri) }
            }

        traySections(sections)
            .drop(NEAR_ROW_COUNT)
            .take(BACKGROUND_ROW_LIMIT)
            .flatMap { section -> section.movies.take(BACKGROUND_ROW_CARDS) }
            .forEach { enqueueCard(context, it.posterUri) }
    }

    private fun traySections(sections: List<HomeSection>): List<HomeSection> =
        sections.filter { it.type == HomeSectionType.Row || it.type == HomeSectionType.Immersive }

    private fun enqueueShowcase(context: Context, posterUri: String) {
        if (posterUri.isBlank()) return
        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(BrewImageUrl.forShowcase(posterUri))
                .size(BrewImageUrl.SHOWCASE_WIDTH, BrewImageUrl.SHOWCASE_HEIGHT)
                .build(),
        )
    }

    private fun enqueueCard(context: Context, posterUri: String) {
        if (posterUri.isBlank()) return
        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(BrewImageUrl.forCard(posterUri))
                .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
                .build(),
        )
    }
}
