package com.google.jetstream.data.util

import com.google.jetstream.data.entities.MovieLanguageRow
import com.google.jetstream.data.remote.BrewLanguageDto
import com.google.jetstream.data.remote.BrewSubtitleDto

/** Port of mobile-viewer `LanguagesDialog.tsx` row merge. */
object MovieLanguageRows {

    fun build(
        primary: BrewLanguageDto?,
        subtitles: List<BrewSubtitleDto>,
    ): List<MovieLanguageRow> {
        val byLanguage = linkedMapOf<String, MovieLanguageRow>()

        fun put(key: String, displayName: String, audio: Boolean, subs: Boolean) {
            if (key.isBlank()) return
            val existing = byLanguage[key]
            if (existing == null) {
                byLanguage[key] = MovieLanguageRow(
                    key = key,
                    displayName = displayName,
                    hasAudio = audio,
                    hasSubtitles = subs,
                )
            } else {
                byLanguage[key] = existing.copy(
                    hasAudio = existing.hasAudio || audio,
                    hasSubtitles = existing.hasSubtitles || subs,
                    displayName = if (displayName.length > existing.displayName.length) {
                        displayName
                    } else {
                        existing.displayName
                    },
                )
            }
        }

        val primaryLabel = primary?.name?.trim().orEmpty()
        val primaryKey = normalizeKey(primaryLabel)
        if (primaryKey.isNotBlank()) {
            put(primaryKey, toDisplayName(primaryLabel), audio = true, subs = false)
        }

        subtitles.forEach { sub ->
            val label = listOfNotNull(
                sub.languageName,
                sub.name,
                sub.title,
                sub.language,
                sub.srcLang,
                sub.lang,
            ).firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
            val key = normalizeKey(label)
            if (key.isBlank()) return@forEach
            put(key, label.ifBlank { toDisplayName(key) }, audio = false, subs = true)
        }

        val all = byLanguage.values.toList()
        val primaryRow = all.firstOrNull { it.key == primaryKey }
        val rest = all.filter { it.key != primaryKey }
            .sortedBy { it.displayName.lowercase() }
        return if (primaryRow != null) listOf(primaryRow) + rest else rest
    }

    private fun normalizeKey(raw: String): String = raw.trim().lowercase()

    private fun toDisplayName(name: String): String =
        name.split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
}
