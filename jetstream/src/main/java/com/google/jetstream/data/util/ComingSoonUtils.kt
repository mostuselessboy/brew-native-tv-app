package com.google.jetstream.data.util

import com.google.jetstream.data.remote.BrewComingSoonReleaseInfoDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object ComingSoonUtils {

    fun isComingSoon(
        distribution: String?,
        viewerReleaseOption: String? = null,
    ): Boolean {
        if (distribution.equals("coming_soon", ignoreCase = true)) return true
        if (viewerReleaseOption.equals("coming_soon", ignoreCase = true)) return true
        return false
    }

    /** Short hint under hero CTA — mirrors mobile-viewer comingSoonReleaseLabel (compact). */
    fun releaseHint(
        releaseInfo: BrewComingSoonReleaseInfoDto?,
        releaseDate: String? = null,
    ): String? {
        releaseInfo?.vodReleaseTimestamp?.takeIf { it.isNotBlank() }?.let { ts ->
            runCatching {
                val normalized = ts.trim().let { raw ->
                    when {
                        raw.endsWith('Z') || raw.contains('+') -> raw
                        raw.contains('T') -> "${raw}Z"
                        else -> raw
                    }
                }
                val release = Instant.parse(normalized)
                val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                val releaseDay = release.atZone(ZoneId.systemDefault()).toLocalDate()
                val days = ChronoUnit.DAYS.between(today, releaseDay)
                when {
                    days <= 0 -> "Coming soon"
                    days == 1L -> "Coming tomorrow"
                    days in 2..6 -> "Coming this ${releaseDay.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }}"
                    else -> {
                        val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                        "Coming ${releaseDay.format(fmt)}"
                    }
                }
            }.getOrNull()?.let { return it }
        }
        releaseDate?.takeIf { it.isNotBlank() }?.let {
            return "Coming soon"
        }
        return null
    }
}
