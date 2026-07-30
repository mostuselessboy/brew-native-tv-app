package com.google.jetstream.data.util

import com.google.jetstream.data.remote.BrewPricingDataDto

/** Port of vod-frontend `utils/vodViewerMonetization.resolveCardCommerceChrome`. */
data class CardCommerceChrome(
    val showStore: Boolean,
    val showPlus: Boolean,
)

object CardCommerce {

    fun resolve(
        monetizationModel: List<String> = emptyList(),
        pricingData: BrewPricingDataDto? = null,
        isSvod: Boolean? = null,
        isTvod: Boolean? = null,
        availableForBuy: Boolean? = null,
        availableForRent: Boolean? = null,
        isStoreContent: Boolean? = null,
    ): CardCommerceChrome {
        val models = monetizationModel.map { it.lowercase().trim() }.filter { it.isNotEmpty() }
        if (models.isNotEmpty()) {
            return CardCommerceChrome(
                showStore = models.contains("tvod"),
                showPlus = models.contains("svod"),
            )
        }

        val hasBuyOrRent = (pricingData?.buy?.isNotEmpty() == true) ||
            (pricingData?.rent?.isNotEmpty() == true)

        return CardCommerceChrome(
            showStore = isTvod == true ||
                availableForBuy == true ||
                availableForRent == true ||
                isStoreContent == true ||
                hasBuyOrRent,
            showPlus = isSvod == true,
        )
    }
}
