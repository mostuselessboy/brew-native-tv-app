package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.EndScreenRecommendation
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieSubscriptionPlan
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.data.remote.BrewCheckPurchaseResponse
import com.google.jetstream.data.util.DetailCtaKind

interface PlaybackRepository {
    suspend fun checkPurchase(
        userId: Int,
        cvName: String,
        campaignVersionId: Int? = null,
    ): BrewCheckPurchaseResponse?

    suspend fun prepareFeaturePlayback(
        movie: MovieDetails,
        checkPurchase: BrewCheckPurchaseResponse?,
        userId: Int,
    ): Result<PlaybackIntent>

    suspend fun prepareTrailerPlayback(movie: MovieDetails, userId: Int): PlaybackIntent?

    suspend fun prepareDirectPlayback(
        userId: Int,
        cvName: String,
        vodAssetId: Int,
        title: String = "",
        initialTimeMs: Long = 0L,
        campaignVersionId: Int? = null,
    ): Result<PlaybackIntent>

    suspend fun fetchEndscreenRecommendations(
        campaignId: Int,
        projectType: String,
        userId: Int?,
        country: String,
    ): List<EndScreenRecommendation>

    suspend fun fetchSubscriptionPlans(country: String): List<MovieSubscriptionPlan>

    suspend fun updateVideoSettings(
        userId: Int,
        vodAssetId: Int,
        initialTimeSeconds: Double,
        percentageWatched: Double,
        watchTimeDelta: Double? = null,
    ): Result<Unit>

    fun isWatchCta(kind: DetailCtaKind): Boolean
}
