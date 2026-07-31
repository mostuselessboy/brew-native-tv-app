package com.google.jetstream.data.util

import com.google.jetstream.data.entities.Movie

/** Port of mobile-viewer `showcaseCta.computeShowcasePrimaryCtaLabel` (TV — no live entitlement). */
data class ShowcasePrimaryCta(
    val label: String,
    val comingSoonHint: String? = null,
)

object ShowcaseCta {

    fun primaryCta(movie: Movie): ShowcasePrimaryCta {
        if (movie.isComingSoon) {
            val hint = movie.comingSoonHint?.trim().orEmpty()
            val label = when {
                hint.isBlank() -> "Coming soon"
                hint.startsWith("Coming", ignoreCase = true) -> hint
                else -> "Coming on $hint"
            }
            return ShowcasePrimaryCta(label = label)
        }
        val projectType = movie.projectType?.lowercase()?.trim().orEmpty()
        if (projectType == "short-film" || movie.isFreeTier) {
            return ShowcasePrimaryCta(label = "Watch for free")
        }
        if (movie.showBrewPlus) {
            return ShowcasePrimaryCta(label = "Subscribe")
        }
        movie.rentPriceFormatted?.takeIf { it.isNotBlank() }?.let { price ->
            return ShowcasePrimaryCta(label = "Rent $price")
        }
        if (movie.showStore) {
            return ShowcasePrimaryCta(label = "Rent")
        }
        return ShowcasePrimaryCta(label = "Watch Now")
    }
}
