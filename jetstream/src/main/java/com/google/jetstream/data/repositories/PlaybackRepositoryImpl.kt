package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.EndScreenAction
import com.google.jetstream.data.entities.EndScreenRecommendation
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieSubscriptionPlan
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.data.remote.BrewCheckPurchaseResponse
import com.google.jetstream.data.remote.BrewEndscreenRecommendationCard
import com.google.jetstream.data.remote.BrewMappers.toMovieSubscriptionPlan
import com.google.jetstream.data.remote.BrewStartPlaybackRequest
import com.google.jetstream.data.remote.BrewUpdateVideoSettingsRequest
import com.google.jetstream.data.remote.BrewVideoSettingsPayload
import com.google.jetstream.data.remote.BrewVodApiService
import com.google.jetstream.data.remote.BrewVodAssetData
import com.google.jetstream.data.util.DetailCtaKind
import com.google.jetstream.data.util.BunnyStream
import com.google.jetstream.data.util.VodPlaybackUrl
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

@Singleton
class PlaybackRepositoryImpl @Inject constructor(
    private val vodApi: BrewVodApiService,
) : PlaybackRepository {

    override suspend fun checkPurchase(
        userId: Int,
        cvName: String,
        campaignVersionId: Int?,
    ): BrewCheckPurchaseResponse? = runCatching {
        vodApi.checkPurchase(
            userId = userId,
            cvName = cvName,
            campaignVersionId = campaignVersionId,
        ).data
    }.getOrNull()

    override suspend fun prepareFeaturePlayback(
        movie: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
        userId: Int,
    ): Result<PlaybackIntent> = runCatching {
        val cvName = movie.cvName.ifBlank { movie.id }
        val vodAssetId = resolveVodAssetId(movie, checkPurchase)

        if (
            checkPurchase?.hasRented == true &&
            checkPurchase.purchaseDetails?.firstPlaybackAt.isNullOrBlank()
        ) {
            runCatching {
                vodApi.startPlayback(
                    BrewStartPlaybackRequest(
                        userId = userId,
                        cvName = cvName,
                        campaignVersionId = movie.campaignVersionId,
                    ),
                )
            }
        }

        val asset = fetchVodAsset(
            userId = userId,
            vodAssetId = vodAssetId,
            cvName = cvName,
            campaignVersionId = movie.campaignVersionId,
        ) ?: error("Unable to load playback asset")

        if (!asset.hasAccess) {
            error("Playback not available for this title")
        }

        val hlsUrl = VodPlaybackUrl.buildHlsUrl(asset)
        if (hlsUrl.isBlank()) {
            error("Missing stream URL")
        }

        PlaybackIntent(
            movieSlug = movie.id,
            title = asset.title?.takeIf { it.isNotBlank() } ?: movie.name,
            hlsUrl = hlsUrl,
            initialTimeMs = resolveInitialTimeMs(checkPurchase, asset),
            isTrailer = false,
            vodAssetId = vodAssetId,
            bunnyVideoId = asset.bunnyVideoId.takeIf { it.isNotBlank() },
            bunnyLibraryId = asset.bunnyLibraryId.takeIf { it.isNotBlank() },
            bunnyCdnZone = asset.bunnyCdnZone.takeIf { it.isNotBlank() },
            licenseServerUrl = asset.licenseServerUrl?.takeIf { it.isNotBlank() }
                ?: BunnyStream.widevineLicenseUrl(asset.bunnyLibraryId, asset.bunnyVideoId),
            isDrm = asset.isDrm,
        )
    }

    override suspend fun prepareTrailerPlayback(movie: MovieDetails, userId: Int): PlaybackIntent? {
        val assetId = movie.trailerVodAssetId?.takeIf { it > 0 } ?: return null

        val asset = fetchVodAsset(
            userId = userId.coerceAtLeast(0),
            vodAssetId = assetId,
            cvName = movie.cvName.ifBlank { movie.id },
            campaignVersionId = movie.campaignVersionId,
        ) ?: return null

        val hlsUrl = VodPlaybackUrl.buildHlsUrl(asset)
        if (hlsUrl.isBlank()) return null

        val licenseUrl = asset.licenseServerUrl?.takeIf { it.isNotBlank() }
            ?: BunnyStream.widevineLicenseUrl(asset.bunnyLibraryId, asset.bunnyVideoId)

        return PlaybackIntent(
            movieSlug = movie.id,
            title = "${movie.name} - Trailer",
            hlsUrl = hlsUrl,
            initialTimeMs = 0L,
            isTrailer = true,
            vodAssetId = assetId,
            bunnyVideoId = asset.bunnyVideoId.takeIf { it.isNotBlank() },
            bunnyLibraryId = asset.bunnyLibraryId.takeIf { it.isNotBlank() },
            bunnyCdnZone = asset.bunnyCdnZone.takeIf { it.isNotBlank() },
            licenseServerUrl = licenseUrl,
            isDrm = movie.trailerIsDrm || asset.isDrm,
        )
    }

    override suspend fun prepareDirectPlayback(
        userId: Int,
        cvName: String,
        vodAssetId: Int,
        title: String,
        initialTimeMs: Long,
        campaignVersionId: Int?,
    ): Result<PlaybackIntent> = runCatching {
        val asset = fetchVodAsset(
            userId = userId,
            vodAssetId = vodAssetId,
            cvName = cvName,
            campaignVersionId = campaignVersionId,
        ) ?: error("Unable to load playback asset")

        if (!asset.hasAccess) {
            error("Playback not available for this title")
        }
        if (asset.allowedInRegion == false) {
            error("This title is not available in your region")
        }

        val hlsUrl = VodPlaybackUrl.buildHlsUrl(asset)
        if (hlsUrl.isBlank()) {
            error("Missing stream URL")
        }

        val resumeMs = when {
            initialTimeMs > 0L -> initialTimeMs
            else -> {
                val progress = asset.progress?.currentTime ?: 0.0
                if (progress > 0.0) (progress * 1000).toLong() else 0L
            }
        }

        PlaybackIntent(
            movieSlug = cvName,
            title = asset.title?.takeIf { it.isNotBlank() } ?: title.ifBlank { cvName },
            hlsUrl = hlsUrl,
            initialTimeMs = resumeMs,
            isTrailer = false,
            vodAssetId = vodAssetId,
            bunnyVideoId = asset.bunnyVideoId.takeIf { it.isNotBlank() },
            bunnyLibraryId = asset.bunnyLibraryId.takeIf { it.isNotBlank() },
            bunnyCdnZone = asset.bunnyCdnZone.takeIf { it.isNotBlank() },
            licenseServerUrl = asset.licenseServerUrl?.takeIf { it.isNotBlank() }
                ?: BunnyStream.widevineLicenseUrl(asset.bunnyLibraryId, asset.bunnyVideoId),
            isDrm = asset.isDrm,
        )
    }

    override suspend fun fetchEndscreenRecommendations(
        campaignId: Int,
        projectType: String,
        userId: Int?,
        country: String,
    ): List<EndScreenRecommendation> = runCatching {
        val data = vodApi.getEndscreenRecommendations(
            campaignId = campaignId,
            projectType = projectType.trim(),
            userId = userId?.takeIf { it > 0 },
            country = country.lowercase().ifBlank { "in" },
        ).data ?: return emptyList()

        listOfNotNull(
            data.primaryCard?.toRecommendation(),
            data.secondaryCard?.toRecommendation(),
        )
    }.getOrDefault(emptyList())

    override suspend fun fetchSubscriptionPlans(country: String): List<MovieSubscriptionPlan> =
        runCatching {
            vodApi.getSubscriptionPlans(
                country = country.lowercase().ifBlank { "in" },
            ).data?.plans.orEmpty().map { it.toMovieSubscriptionPlan() }
        }.getOrDefault(emptyList())

    private fun BrewEndscreenRecommendationCard.toRecommendation(): EndScreenRecommendation? {
        val card = cardData ?: return null
        val slug = card.localizedCvName?.takeIf { it.isNotBlank() }
            ?: card.cvName?.takeIf { it.isNotBlank() }
            ?: return null
        val poster = card.projectPoster?.takeIf { it.isNotBlank() }.orEmpty()
        val assetId = card.movieAssetId?.takeIf { it > 0 }
        return EndScreenRecommendation(
            slug = slug,
            title = card.projectTitle?.takeIf { it.isNotBlank() } ?: slug,
            posterUri = poster,
            action = EndScreenAction.fromApi(action),
            vodAssetId = assetId,
            trailerUrl = card.trailerUrl?.takeIf { it.isNotBlank() },
        )
    }

    override suspend fun updateVideoSettings(
        userId: Int,
        vodAssetId: Int,
        initialTimeSeconds: Double,
        percentageWatched: Double,
        watchTimeDelta: Double?,
    ): Result<Unit> = runCatching {
        vodApi.updateVideoSettings(
            BrewUpdateVideoSettingsRequest(
                userId = userId,
                vodAssetId = vodAssetId,
                videoSettings = BrewVideoSettingsPayload(
                    initialTime = initialTimeSeconds,
                    volume = 1.0,
                    watchTimeDelta = watchTimeDelta,
                    percentageWatched = percentageWatched,
                ),
            ),
        )
        Unit
    }

    override fun isWatchCta(kind: DetailCtaKind): Boolean = when (kind) {
        DetailCtaKind.WatchForFree,
        DetailCtaKind.WatchNow,
        DetailCtaKind.ContinueWatching -> true
        else -> false
    }

    private suspend fun fetchVodAsset(
        userId: Int,
        vodAssetId: Int?,
        cvName: String,
        campaignVersionId: Int?,
    ): BrewVodAssetData? {
        val response = runCatching {
            vodApi.getVodAsset(
                userId = userId,
                vodAssetId = vodAssetId?.takeIf { it > 0 },
                cvName = cvName.takeIf { vodAssetId == null || vodAssetId <= 0 },
                campaignVersionId = campaignVersionId,
            ).data
        }.getOrNull()
        return response?.takeIf { it.bunnyVideoId.isNotBlank() && it.bunnyCdnZone.isNotBlank() }
    }

    private fun resolveVodAssetId(
        movie: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
    ): Int? {
        checkPurchase?.videoSettings?.vodAssetId?.asInt()?.takeIf { it > 0 }?.let { return it }
        movie.vodAssetId?.takeIf { it > 0 }?.let { return it }
        return null
    }

    private fun resolveInitialTimeMs(
        checkPurchase: BrewCheckPurchaseResponse?,
        asset: BrewVodAssetData,
    ): Long {
        val settings = checkPurchase?.videoSettings
        val fromSettings = listOfNotNull(
            settings?.initialTime?.asDouble(),
            settings?.initialTimeAlt?.asDouble(),
        ).firstOrNull { it > 0.0 }
        if (fromSettings != null) {
            return (fromSettings * 1000).toLong()
        }
        val progress = asset.progress?.currentTime ?: 0.0
        return if (progress > 0.0) (progress * 1000).toLong() else 0L
    }

    private fun JsonElement.asDouble(): Double? = when (this) {
        is JsonPrimitive -> {
            content.toDoubleOrNull()
                ?: content.toLongOrNull()?.toDouble()
                ?: intOrNull?.toDouble()
                ?: doubleOrNull
        }
        else -> null
    }

    private fun JsonElement.asInt(): Int? = when (this) {
        is JsonPrimitive -> {
            intOrNull
                ?: content.toLongOrNull()?.toInt()
                ?: content.toDoubleOrNull()?.toInt()
        }
        else -> null
    }
}
