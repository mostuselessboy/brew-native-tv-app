package com.google.jetstream.data.util

/** Port of vod-frontend / mobile-viewer `movieRibbonLabel.ts`. */
object RibbonLabel {

    fun fromValue(value: String?): String? =
        value?.trim()?.takeIf { it.isNotBlank() }

    fun normalize(label: String): String =
        label.trim().replace(Regex("\\s+"), " ")

    fun isFilmOfTheWeek(label: String): Boolean =
        normalize(label).lowercase() == "film of the week"

    fun isShortOfTheWeek(label: String): Boolean =
        normalize(label).lowercase() == "short of the week"

    fun isEarlyAccess(label: String): Boolean =
        normalize(label).lowercase() == "early access"

    fun isBrewExclusive(label: String): Boolean =
        normalize(label).lowercase() == "brew exclusive"

    fun isSelectionStamp(label: String): Boolean =
        isEarlyAccess(label) || isBrewExclusive(label)

    fun isTheShortList(label: String): Boolean =
        normalize(label).lowercase() == "the short list"

    fun resolve(
        label: String?,
        projectType: String? = null,
    ): String? {
        val normalized = fromValue(label) ?: return null
        if (projectType == "short-film" && isTheShortList(normalized)) return null
        return normalized
    }
}

object RibbonLabelImages {
    const val FILM_OF_THE_WEEK = "https://www.brew.tv/images/film-of-the-week.png"
    const val SHORT_OF_THE_WEEK = "https://www.brew.tv/images/short-of-the-week.png"
}
