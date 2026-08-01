package com.google.jetstream.data.util

import com.google.jetstream.data.entities.PlaybackSubtitle
import com.google.jetstream.data.remote.BrewCampaignSubtitleRowDto

object PlaybackSubtitleMapper {

    fun fromCampaignRows(rows: List<BrewCampaignSubtitleRowDto>): List<PlaybackSubtitle> {
        return rows.mapNotNull { row ->
            val url = resolveUrl(row) ?: return@mapNotNull null
            val language = resolveLanguage(row)
            val label = resolveLabel(row, language)
            PlaybackSubtitle(
                id = language,
                language = language,
                label = label,
                url = url,
                isDefault = row.default == true,
            )
        }.distinctBy { it.language }
    }

    private fun resolveUrl(row: BrewCampaignSubtitleRowDto): String? {
        row.url?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
        val file = row.file?.trim().orEmpty()
        if (file.isBlank() || file.equals("null", ignoreCase = true)) return null
        if (
            file.contains(".vtt", ignoreCase = true) ||
            file.contains(".webvtt", ignoreCase = true) ||
            file.contains("/captions/", ignoreCase = true)
        ) {
            return file
        }
        return null
    }

    private fun resolveLanguage(row: BrewCampaignSubtitleRowDto): String {
        listOf(row.language, row.srcLang, row.lang)
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
            ?.lowercase()
            ?.let { return it }

        val name = listOf(row.languageName, row.name, row.label, row.title)
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
            .orEmpty()
        if (name.isNotBlank()) {
            return name.lowercase().replace(Regex("\\s+"), "-").substringBefore('-')
        }
        return "en"
    }

    private fun resolveLabel(row: BrewCampaignSubtitleRowDto, language: String): String {
        return listOf(row.languageName, row.name, row.label, row.title, language)
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
            ?: language
    }
}
