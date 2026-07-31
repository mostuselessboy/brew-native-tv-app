package com.google.jetstream.data.util

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Relative time label — parity with vod `formatTimeAgo`. */
fun formatTimeAgo(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    val instant = runCatching { Instant.parse(isoDate) }.getOrNull() ?: return ""
    val now = Instant.now()
    val minutes = ChronoUnit.MINUTES.between(instant, now).coerceAtLeast(0)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        minutes < 60 * 24 * 30 -> "${minutes / (60 * 24)}d ago"
        minutes < 60 * 24 * 365 -> "${minutes / (60 * 24 * 30)}mo ago"
        else -> "${minutes / (60 * 24 * 365)}y ago"
    }.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
    }.lowercase(Locale.US)
}
