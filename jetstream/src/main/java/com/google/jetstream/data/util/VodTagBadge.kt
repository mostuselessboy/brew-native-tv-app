package com.google.jetstream.data.util

/** Port of vod-frontend `lib/vodTagBadge.ts`. */
object VodTagBadge {

    private val directTags = setOf("new", "trending", "hot")

    fun normalize(vodTag: String?): String = vodTag.orEmpty().trim().lowercase()

    fun isFestivalStyle(vodTag: String?): Boolean {
        val normalized = normalize(vodTag)
        return normalized.isNotEmpty() && normalized !in directTags
    }

    fun movieCardLabel(vodTag: String?): String? {
        val normalized = normalize(vodTag)
        if (normalized.isEmpty()) return null
        return when (normalized) {
            "new" -> "New"
            "trending" -> "Trending"
            "hot" -> "Hot"
            else -> formatTag(vodTag.orEmpty())
        }
    }

    fun showcaseLabel(vodTag: String?): String? {
        val normalized = normalize(vodTag)
        if (normalized.isEmpty()) return null
        return when (normalized) {
            "new" -> "New Release"
            "trending" -> "Trending now"
            "hot" -> "Hot"
            else -> formatTag(vodTag.orEmpty())
        }
    }

    private fun formatTag(vodTag: String): String {
        return vodTag.trim()
            .replace('_', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { ch -> ch.uppercaseChar() }
            }
    }
}
