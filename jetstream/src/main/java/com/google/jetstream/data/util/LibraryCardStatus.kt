package com.google.jetstream.data.util

import com.google.jetstream.data.remote.BrewLibraryCampaignInfoDto
import com.google.jetstream.data.remote.BrewLibraryOrderDto
import com.google.jetstream.data.remote.BrewLibraryWatchProgressDto

/** Mirrors mobile-viewer `libraryCardStatus.ts`. */
enum class LibraryClickAction {
    OpenMoviePage,
    Play,
    Nothing,
    ;

    companion object {
        fun fromApi(value: String?): LibraryClickAction? = when (value) {
            "open_movie_page" -> OpenMoviePage
            "play" -> Play
            "nothing" -> Nothing
            else -> null
        }
    }
}

object LibraryCardStatus {

    data class Resolved(
        val clickAction: LibraryClickAction,
        val unavailable: Boolean,
    )

    fun resolve(order: BrewLibraryOrderDto): Resolved {
        val unavailable = isContentUnavailable(order.campaignInfo)
        var clickAction = resolveClickActionFallback(order)
        if (unavailable) {
            clickAction = LibraryClickAction.Nothing
        }
        return Resolved(clickAction = clickAction, unavailable = unavailable)
    }

    private fun isContentUnavailable(info: BrewLibraryCampaignInfoDto?): Boolean = false

    private fun resolveClickActionFallback(order: BrewLibraryOrderDto): LibraryClickAction {
        LibraryClickAction.fromApi(order.clickAction)?.let { return it }

        if (isContentUnavailable(order.campaignInfo)) {
            return LibraryClickAction.Nothing
        }

        val rentExpired = order.purchaseType == "rent" &&
            order.accessType == null &&
            order.watchProgress == null

        val progress = order.watchProgress
        val initial = LibraryWatchProgress.resolveInitialTimeSeconds(progress)
        val pct = LibraryWatchProgress.resolvePercentageWatched(progress)
        val inProgress = initial > 0 || (pct in 1..94)

        val info = order.campaignInfo
        val entitled =
            info?.isSvod == true ||
                order.accessType == "free" ||
                order.accessType == "buy" ||
                (order.purchaseType == "rent" && !rentExpired) ||
                (order.purchaseType == "buy" && order.accessType == null)

        if (inProgress && entitled) return LibraryClickAction.Play
        return LibraryClickAction.OpenMoviePage
    }
}
