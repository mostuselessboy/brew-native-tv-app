package com.google.jetstream.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/** Port of vod-frontend `formatReleaseYear` + leave-soon check. */
object BrewDateUtils {

    fun formatReleaseYear(releaseDate: String?): String? {
        if (releaseDate.isNullOrBlank()) return null
        val trimmed = releaseDate.trim()
        if (trimmed == "null" || trimmed == "undefined") return null

        val yearPrefix = Regex("""^(\d{4})""").find(trimmed)?.groupValues?.getOrNull(1)
        if (yearPrefix != null) {
            val y = yearPrefix.toIntOrNull()
            if (y != null && y > 0) return y.toString()
        }

        return try {
            val instant = Instant.parse(trimmed)
            val year = instant.atZone(ZoneOffset.UTC).year
            if (year > 0) year.toString() else null
        } catch (_: DateTimeParseException) {
            try {
                val year = LocalDate.parse(trimmed.take(10)).year
                if (year > 0) year.toString() else null
            } catch (_: Exception) {
                null
            }
        }
    }

    fun isLeaveDateInFuture(leaveDate: String?): Boolean {
        if (leaveDate.isNullOrBlank()) return false
        return try {
            val leave = Instant.parse(leaveDate.trim())
            leave.isAfter(Instant.now())
        } catch (_: Exception) {
            try {
                val leave = LocalDate.parse(leaveDate.trim().take(10))
                leave.isAfter(LocalDate.now(ZoneOffset.UTC))
            } catch (_: Exception) {
                false
            }
        }
    }
}
