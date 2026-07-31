package com.google.jetstream.data.util

import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.remote.BrewCheckPurchaseResponse
import com.google.jetstream.data.remote.BrewPurchaseCtaDto
import com.google.jetstream.data.remote.BrewPurchaseCtaSlotDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/** Mirrors mobile-viewer `effectivePurchaseCta.ts`. */
object EffectivePurchaseCta {

    fun resolve(
        movie: MovieDetails,
        purchase: BrewCheckPurchaseResponse?,
    ): BrewPurchaseCtaDto? {
        if (purchase == null) return null
        purchase.purchaseCta?.let { return it }
        if (purchaseGrantsPlayback(purchase)) {
            return buildEntitledFallback(purchase)
        }
        return null
    }

    fun mergePurchaseCtaSlots(
        movie: MovieDetails,
        purchase: BrewCheckPurchaseResponse?,
    ): List<DetailPurchaseCtaSlot> {
        val effective = resolve(movie, purchase) ?: return movie.purchaseCtaSlots
        val plans = purchase?.subscriptionPlans ?: emptyList()
        return DetailPurchaseCta.mapFromApi(
            purchaseCta = effective,
            rentPriceFormatted = movie.rentPriceFormatted,
            buyPriceFormatted = movie.buyPriceFormatted,
            rentOriginalPriceFormatted = movie.rentOriginalPriceFormatted,
            subscriptionPlans = plans,
            comingSoonHint = movie.comingSoonHint,
        ).ifEmpty { movie.purchaseCtaSlots }
    }

    private fun purchaseGrantsPlayback(purchase: BrewCheckPurchaseResponse): Boolean =
        purchase.hasPurchased ||
            purchase.hasRented ||
            purchase.hasFreePlaybackAccess == true ||
            purchase.isOwnFilm ||
            purchase.hasActiveSubscription == true

    private fun buildEntitledFallback(purchase: BrewCheckPurchaseResponse): BrewPurchaseCtaDto {
        val settings = purchase.videoSettings
        val percent = settings?.percentageWatched?.asDouble()?.toInt()
        val seekTime = listOfNotNull(
            settings?.initialTime?.asDouble(),
            settings?.initialTimeAlt?.asDouble(),
        ).firstOrNull { it > 0.0 } ?: 0.0
        val isContinueWatching = (percent ?: 0) > 0 || seekTime > 0.0
        val vodAssetId = settings?.vodAssetId?.asInt()

        return BrewPurchaseCtaDto(
            scenario = "svod-tvod",
            slots = listOf(
                BrewPurchaseCtaSlotDto(
                    kind = "watch",
                    color = "yellow",
                    free = false,
                    isContinueWatching = isContinueWatching,
                    percentageWatched = percent?.takeIf { it > 0 },
                ),
            ),
        )
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
