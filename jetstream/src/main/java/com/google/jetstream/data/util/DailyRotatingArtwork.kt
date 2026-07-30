package com.google.jetstream.data.util

/**
 * Port of vod-frontend `dailyRotatingArtwork.ts` — UTC-day rotation across
 * primary landscape/vertical art + thumbnail alternates.
 */
object DailyRotatingArtwork {

    private const val UTC_DAY_MS = 24L * 60L * 60L * 1000L

    fun getUtcArtworkDay(nowMs: Long = System.currentTimeMillis()): Long =
        nowMs / UTC_DAY_MS

    fun normalizeUrl(source: String?): String? =
        source?.trim()?.takeIf { it.isNotEmpty() }

    fun dedupe(urls: List<String?>): List<String> {
        val seen = LinkedHashSet<String>()
        val out = ArrayList<String>()
        for (raw in urls) {
            val url = normalizeUrl(raw) ?: continue
            if (seen.add(url)) out.add(url)
        }
        return out
    }

    fun buildLandscapePool(
        backgroundArtUrl: String?,
        horizontalThumbnails: List<String> = emptyList(),
    ): List<String> {
        val primary = normalizeUrl(backgroundArtUrl)
        val alternates = dedupe(horizontalThumbnails).filter { it != primary }
        return if (primary != null) listOf(primary) + alternates else alternates
    }

    fun buildVerticalPool(
        verticalBackgroundArtUrl: String?,
        verticalThumbnails: List<String> = emptyList(),
    ): List<String> {
        val primary = normalizeUrl(verticalBackgroundArtUrl)
        val alternates = dedupe(verticalThumbnails).filter { it != primary }
        return if (primary != null) listOf(primary) + alternates else alternates
    }

    fun pickFromPool(pool: List<String>, nowMs: Long = System.currentTimeMillis()): String {
        if (pool.isEmpty()) return ""
        val day = getUtcArtworkDay(nowMs)
        return pool[(day % pool.size).toInt()]
    }

    fun pickDailyLandscape(
        backgroundArtUrl: String?,
        horizontalThumbnails: List<String> = emptyList(),
        fallback: String? = null,
        nowMs: Long = System.currentTimeMillis(),
    ): String {
        val pool = buildLandscapePool(backgroundArtUrl, horizontalThumbnails)
        val picked = pickFromPool(pool, nowMs)
        return picked.ifBlank { normalizeUrl(fallback).orEmpty() }
    }

    fun pickDailyVertical(
        verticalBackgroundArtUrl: String?,
        verticalThumbnails: List<String> = emptyList(),
        fallback: String? = null,
        nowMs: Long = System.currentTimeMillis(),
    ): String {
        val pool = buildVerticalPool(verticalBackgroundArtUrl, verticalThumbnails)
        val picked = pickFromPool(pool, nowMs)
        return picked.ifBlank { normalizeUrl(fallback).orEmpty() }
    }
}
