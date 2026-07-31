package com.google.jetstream.data.remote

import com.google.jetstream.data.entities.LibraryCardLayout
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.LibraryShelf
import com.google.jetstream.data.entities.LibraryShelfId
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MyLibraryPage
import com.google.jetstream.data.util.BrewArtworkUrls
import com.google.jetstream.data.util.LibraryCardStatus
import com.google.jetstream.data.util.LibraryClickAction
import com.google.jetstream.data.util.LibraryWatchProgress
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal object LibraryMappers {

    private val portraitShelves = setOf(
        LibraryShelfId.RateShare,
        LibraryShelfId.Bookmarks,
    )

    private val hiddenWhenEmpty = setOf(
        LibraryShelfId.RateShare,
        LibraryShelfId.Bookmarks,
    )

    private val shelfTitles = mapOf(
        LibraryShelfId.ContinueWatching to "Continue Watching",
        LibraryShelfId.RateShare to "Titles you finished",
        LibraryShelfId.Purchased to "Purchased",
        LibraryShelfId.ExpiredPurchases to "Expired Purchases",
        LibraryShelfId.Rented to "Rent",
        LibraryShelfId.Bookmarks to "Bookmarks",
        LibraryShelfId.RentedExpired to "Rent expired",
    )

    private val shelfOrder = listOf(
        LibraryShelfId.ContinueWatching,
        LibraryShelfId.RateShare,
        LibraryShelfId.Purchased,
        LibraryShelfId.ExpiredPurchases,
        LibraryShelfId.Rented,
        LibraryShelfId.Bookmarks,
        LibraryShelfId.RentedExpired,
    )

    fun parseMyLibrary(response: BrewMyLibraryResponse, json: Json): MyLibraryPage {
        val rowById = response.rows.associateBy { it.id }
        val shelves = shelfOrder.mapNotNull { shelfId ->
            val row = rowById[shelfId.apiId] ?: return@mapNotNull null
            val items = parseRowItems(row, shelfId, json)
            LibraryShelf(
                id = shelfId,
                title = shelfTitles[shelfId].orEmpty(),
                items = items,
                total = row.total,
                hasMore = row.hasMore,
                offset = row.offset + items.size,
            )
        }
        val wholeEmpty = shelves.all { it.items.isEmpty() }
        val visibleShelves = shelves.filter { shelf ->
            if (wholeEmpty) return@filter false
            if (shelf.items.isNotEmpty()) return@filter true
            shelf.id !in hiddenWhenEmpty
        }
        return MyLibraryPage(
            shelves = visibleShelves,
            hasActiveSubscription = response.meta?.hasActiveSubscription == true,
        )
    }

    fun parseRowItems(
        row: BrewMyLibraryRowDto,
        shelfId: LibraryShelfId,
        json: Json,
    ): List<LibraryItem> {
        val layout = if (shelfId in portraitShelves) {
            LibraryCardLayout.Portrait
        } else {
            LibraryCardLayout.Landscape
        }
        return when (shelfId) {
            LibraryShelfId.Bookmarks -> row.items.mapNotNull { element ->
                runCatching {
                    json.decodeFromJsonElement<BrewBookmarkItemDto>(element).toLibraryItem(layout)
                }.getOrNull()
            }
            else -> row.items.mapNotNull { element ->
                runCatching {
                    json.decodeFromJsonElement<BrewLibraryOrderDto>(element).toLibraryItem(layout)
                }.getOrNull()
            }
        }
    }

    private fun BrewLibraryOrderDto.toLibraryItem(layout: LibraryCardLayout): LibraryItem? {
        val info = campaignInfo ?: return null
        val slug = info.localizedCvName?.takeIf { it.isNotBlank() }
            ?: info.cvName?.takeIf { it.isNotBlank() }
            ?: return null
        val poster = resolveLandscapePoster(info)
        val progress = LibraryWatchProgress.resolvePercentageWatched(watchProgress)
        val initialTime = LibraryWatchProgress.resolveInitialTimeSeconds(watchProgress)
        val episodeLabel = LibraryWatchProgress.formatEpisodeLabel(watchProgress)
        val subtitle = LibraryWatchProgress.cardSubtitle(
            watchProgress,
            info.shortDescription ?: info.projectSynopsis.orEmpty(),
        )
        val cardStatus = LibraryCardStatus.resolve(this)
        val vodAssetId = watchProgress?.vodAssetId?.takeIf { it > 0 }
        return LibraryItem(
            movieId = slug,
            movie = Movie(
                id = slug,
                videoUri = "",
                subtitleUri = null,
                posterUri = poster,
                name = info.projectTitle.orEmpty().ifBlank { slug },
                description = subtitle,
                country = info.country,
                genres = info.genre?.let { listOf(it) }.orEmpty(),
                showStore = info.isStoreContent == true,
                showBrewPlus = info.isSvod == true,
                projectType = info.projectType,
                watchProgressPercent = progress.takeIf { it > 0 },
                vodAssetId = vodAssetId,
                initialTimeSeconds = initialTime.takeIf { it > 0 },
                libraryClickAction = cardStatus.clickAction,
            ),
            progressPercent = progress,
            episodeLabel = episodeLabel,
            layout = layout,
            vodAssetId = vodAssetId,
            initialTimeSeconds = initialTime,
            clickAction = cardStatus.clickAction,
        )
    }

    private fun BrewBookmarkItemDto.toLibraryItem(layout: LibraryCardLayout): LibraryItem? {
        val info = campaignInfo
        val slug = slug?.takeIf { it.isNotBlank() }
            ?: info?.localizedCvName?.takeIf { it.isNotBlank() }
            ?: info?.cvName?.takeIf { it.isNotBlank() }
            ?: return null
        val poster = projectPosterUrl?.takeIf { it.isNotBlank() }
            ?: info?.projectPoster?.takeIf { it.isNotBlank() }
            ?: resolvePortraitPoster(info)
            ?: resolveLandscapePoster(info)
        val title = info?.projectTitle?.takeIf { it.isNotBlank() }
            ?: projectTitle?.takeIf { it.isNotBlank() }
            ?: assetTitle?.takeIf { it.isNotBlank() }
            ?: slug
        return LibraryItem(
            movieId = slug,
            movie = Movie(
                id = slug,
                videoUri = "",
                subtitleUri = null,
                posterUri = poster,
                name = title,
                description = assetTitle.orEmpty(),
                projectType = info?.projectType ?: projectType,
            ),
            layout = layout,
        )
    }

    private fun resolveLandscapePoster(info: BrewLibraryCampaignInfoDto?): String {
        if (info == null) return ""
        return listOfNotNull(
            info.projectPoster?.takeIf { it.isNotBlank() },
            BrewArtworkUrls.asUrl(info.appearance?.backgroundArt),
            BrewArtworkUrls.firstUrl(info.appearance?.horizontalThumbnails),
            BrewArtworkUrls.asUrl(info.appearance?.verticalBackgroundArt),
            BrewArtworkUrls.firstUrl(info.appearance?.verticalThumbnails),
        ).firstOrNull().orEmpty()
    }

    private fun resolvePortraitPoster(info: BrewLibraryCampaignInfoDto?): String? {
        if (info == null) return null
        return listOfNotNull(
            info.projectPoster?.takeIf { it.isNotBlank() },
            BrewArtworkUrls.asUrl(info.appearance?.verticalBackgroundArt),
            BrewArtworkUrls.firstUrl(info.appearance?.verticalThumbnails),
        ).firstOrNull()
    }
}
