package com.google.jetstream.data.util

/**
 * Port of vod-frontend `teaserVideoUrl.ts` — Bunny `/original` → playable MP4.
 * Trailer CDN requires `Referer: https://www.brew.tv/`.
 */
object BrewTrailerUrl {

    const val REFERER = "https://www.brew.tv/"
    const val ORIGIN = "https://www.brew.tv"

    fun toPlayableMp4(url: String?, height: Int = 720): String {
        if (url.isNullOrBlank()) return ""
        val trimmed = url.trim()
        if (trimmed.contains("youtube.com", ignoreCase = true) ||
            trimmed.contains("youtu.be", ignoreCase = true)
        ) {
            return ""
        }

        val suffix = "/play_${height}p.mp4"
        return when {
            Regex("""/original/?$""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ->
                trimmed.replace(Regex("""/original/?$""", RegexOption.IGNORE_CASE), suffix)
            Regex("""\.m3u8(\?.*)?$""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ->
                trimmed.replace(Regex("""/[^/]+\.m3u8(\?.*)?$""", RegexOption.IGNORE_CASE), suffix)
            Regex("""/play_\d+p\.mp4(\?.*)?$""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ->
                trimmed.replace(Regex("""/play_\d+p\.mp4(\?.*)?$""", RegexOption.IGNORE_CASE), suffix)
            trimmed.contains("b-cdn.net", ignoreCase = true) &&
                !Regex("""\.(mp4|webm)(\?.*)?$""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ->
                trimmed.trimEnd('/') + suffix
            else -> trimmed
        }
    }

    fun resolveFromCandidates(vararg candidates: String?): String {
        for (raw in candidates) {
            val playable = toPlayableMp4(raw)
            if (playable.isNotBlank()) return playable
        }
        return ""
    }
}
