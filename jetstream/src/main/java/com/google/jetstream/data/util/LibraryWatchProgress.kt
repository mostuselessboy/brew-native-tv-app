package com.google.jetstream.data.util

import com.google.jetstream.data.remote.BrewLibraryWatchProgressDto

/** Mirrors mobile-viewer `libraryWatchProgressRules.ts`. */
object LibraryWatchProgress {

    private const val FinishedPctFallback = 95
    private const val DurationFinishRatio = 0.9

    fun resolveInitialTimeSeconds(progress: BrewLibraryWatchProgressDto?): Int {
        if (progress == null) return 0
        val fromField = progress.initialTimeSeconds
        if (fromField > 0) return fromField.toInt()
        val fromSettings = progress.videoSettings?.initialTime ?: 0.0
        if (fromSettings > 0) return fromSettings.toInt()
        return 0
    }

    fun resolvePercentageWatched(progress: BrewLibraryWatchProgressDto?): Int {
        if (progress == null) return 0
        val fromField = progress.percentageWatched
        if (fromField > 0) return fromField.toInt().coerceIn(0, 100)
        val fromSettings = progress.videoSettings?.percentageWatched ?: 0.0
        if (fromSettings > 0) return fromSettings.toInt().coerceIn(0, 100)
        val initial = resolveInitialTimeSeconds(progress)
        val duration = progress.durationSeconds ?: 0.0
        if (initial > 0 && duration > 0) {
            return ((initial / duration) * 100).toInt().coerceIn(0, 100)
        }
        return 0
    }

    fun formatEpisodeLabel(progress: BrewLibraryWatchProgressDto?): String? {
        if (progress == null) return null
        val episode = progress.episodeNo ?: return null
        if (episode <= 0) return null
        val season = progress.seasonNo
        return if (season != null && season > 0) {
            "S${season}E$episode"
        } else {
            "E$episode"
        }
    }

    fun cardSubtitle(progress: BrewLibraryWatchProgressDto?, fallback: String): String {
        val episode = formatEpisodeLabel(progress)
        if (!episode.isNullOrBlank()) return episode
        val assetTitle = progress?.assetTitle?.trim().orEmpty()
        if (assetTitle.isNotBlank()) return assetTitle
        return fallback
    }

    private fun isEpisodeAssetType(assetType: String?): Boolean =
        assetType.orEmpty().trim().lowercase() == "episode"

    private fun hasWatchProgress(progress: BrewLibraryWatchProgressDto?): Boolean =
        resolveInitialTimeSeconds(progress) > 0 || resolvePercentageWatched(progress) > 0

    private fun endCreditsThresholdSeconds(progress: BrewLibraryWatchProgressDto?): Double? {
        val credits = progress?.creditsStartTime
        if (credits != null && credits > 0) return credits
        val duration = progress?.durationSeconds
        if (duration != null && duration > 0) return duration * DurationFinishRatio
        return null
    }

    private fun isPastEndCredits(progress: BrewLibraryWatchProgressDto?): Boolean {
        if (progress == null) return false
        val initial = resolveInitialTimeSeconds(progress)
        val threshold = endCreditsThresholdSeconds(progress)
        if (threshold != null && initial > 0) return initial >= threshold
        return resolvePercentageWatched(progress) >= FinishedPctFallback
    }

    private fun isUpNextEpisode(progress: BrewLibraryWatchProgressDto?): Boolean {
        if (progress == null) return false
        if (!progress.finishContentAt.isNullOrBlank()) return false
        if (hasWatchProgress(progress)) return false
        if (!isEpisodeAssetType(progress.assetType)) return false
        return progress.isUpNext == true || progress.videoSettings?.isUpNext == true || true
    }

    /** Continue Watching shelf eligibility — same rules as mobile. */
    fun isContinueWatchingProgress(progress: BrewLibraryWatchProgressDto?): Boolean {
        if (progress == null) return false
        if (isUpNextEpisode(progress)) return true
        if (!hasWatchProgress(progress)) return false
        if (isPastEndCredits(progress)) return false
        if (!progress.finishContentAt.isNullOrBlank()) {
            val initial = resolveInitialTimeSeconds(progress)
            val pct = resolvePercentageWatched(progress)
            if (initial <= 0 && pct <= 0) return false
        }
        return true
    }
}
