package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.remote.BrewApiService
import com.google.jetstream.data.remote.BrewContinueWatchingContentDto
import com.google.jetstream.data.remote.BrewLibraryOrderDto
import com.google.jetstream.data.util.BrewDateUtils
import com.google.jetstream.data.util.VodTagBadge
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val brewApiService: BrewApiService,
) : LibraryRepository {

    override suspend fun getContinueWatching(userId: Int): List<Movie> {
        if (userId <= 0) return emptyList()
        val response = runCatching {
            brewApiService.getContinueWatching(userId = userId.toString())
        }.getOrNull() ?: return emptyList()
        return response.data.mapNotNull { it.contentData?.toLibraryMovie() }
    }

    override suspend fun getMyLibrary(userId: Int): MyLibraryContent {
        if (userId <= 0) return MyLibraryContent()
        val response = runCatching {
            brewApiService.getUserActiveOrders(userId = userId)
        }.getOrNull() ?: return MyLibraryContent()

        val orders = response.data?.orders.orEmpty()
            .ifEmpty { response.orders.orEmpty() }

        val rented = mutableListOf<Movie>()
        val purchased = mutableListOf<Movie>()
        val expired = mutableListOf<Movie>()

        orders.forEach { order ->
            val movie = order.toLibraryMovie() ?: return@forEach
            when {
                order.isExpiredRental() -> expired += movie
                order.purchaseType == "buy" -> purchased += movie
                order.purchaseType == "rent" -> rented += movie
            }
        }

        return MyLibraryContent(
            rented = rented,
            purchased = purchased,
            expired = expired,
        )
    }

    private fun BrewContinueWatchingContentDto.toLibraryMovie(): Movie? {
        val slug = slug?.takeIf { it.isNotBlank() } ?: return null
        val name = projectTitle?.takeIf { it.isNotBlank() } ?: return null
        val poster = backgroundArtUrl?.takeIf { it.isNotBlank() }
            ?: projectPosterUrl?.takeIf { it.isNotBlank() }
            ?: return null
        return Movie(
            id = slug,
            videoUri = "",
            subtitleUri = null,
            posterUri = poster,
            backdropUri = poster,
            name = name,
            description = shortDescription?.takeIf { it.isNotBlank() }
                ?: projectSynopsis.orEmpty(),
            year = BrewDateUtils.formatReleaseYear(releaseDate),
            country = country,
            vodTagLabel = VodTagBadge.movieCardLabel(vodTag),
        )
    }

    private fun BrewLibraryOrderDto.toLibraryMovie(): Movie? {
        val info = campaignInfo ?: return null
        val slug = info.cvName?.takeIf { it.isNotBlank() } ?: return null
        val name = info.projectTitle?.takeIf { it.isNotBlank() } ?: return null
        val poster = info.appearance?.backgroundArt?.url?.takeIf { it.isNotBlank() }
            ?: return null
        val year = BrewDateUtils.formatReleaseYear(info.projectReleaseDate)
        val subtitle = info.shortDescription?.takeIf { it.isNotBlank() }.orEmpty()
        val tag = when {
            purchaseType == "buy" -> "OWNED"
            isExpiredRental() -> "EXPIRED"
            else -> expiryTagLabel()
        }
        return Movie(
            id = slug,
            videoUri = "",
            subtitleUri = null,
            posterUri = poster,
            backdropUri = poster,
            name = name,
            description = subtitle,
            year = year,
            country = info.country,
            vodTagLabel = tag,
            showStore = true,
        )
    }

    private fun BrewLibraryOrderDto.isExpiredRental(): Boolean {
        if (rentalStatus?.isExpired == true) return true
        val expiry = watchExpiresAt ?: orderExpiresAt ?: rentalStatus?.expiresAt
        return expiry?.let { runCatching { Instant.parse(it) }.getOrNull()?.isBefore(Instant.now()) }
            ?: false
    }

    private fun BrewLibraryOrderDto.expiryTagLabel(): String {
        val expiry = watchExpiresAt ?: orderExpiresAt ?: rentalStatus?.expiresAt ?: return "RENTED"
        val instant = runCatching { Instant.parse(expiry) }.getOrNull() ?: return "RENTED"
        val days = ((instant.toEpochMilli() - System.currentTimeMillis()) / (86_400_000L))
            .coerceAtLeast(0)
        return if (days <= 0) "EXPIRED" else "EXPIRES IN ${days}D"
    }
}
