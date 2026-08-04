package com.google.jetstream.data.util

import android.net.Uri
import android.os.Build

/**
 * Bunny CDN image sizing — mirrors vod-frontend `imageUrlOptimizer.ts`.
 * Only rewrites Bunny pull-zone hosts; leaves TMDB / other origins untouched
 * (rewriting those was a common cause of blank cards on TV).
 */
object BrewImageUrl {

    /** Tray cards — ~220dp wide @ 1.5× density. */
    const val CARD_WIDTH = 330
    const val CARD_HEIGHT = 186

    /** Library portrait shelves — ~150×225dp @ 2× density (Bookmarks, Titles you finished). */
    const val PORTRAIT_CARD_WIDTH = 300
    const val PORTRAIT_CARD_HEIGHT = 450

    /** Home showcase hero — ~380dp tall @ 1.5× density. */
    const val SHOWCASE_WIDTH = 1024
    const val SHOWCASE_HEIGHT = 576

    /** Details backdrop. */
    const val DETAIL_WIDTH = 854
    const val DETAIL_HEIGHT = 480

    /** Collection / hero backdrops — full-bleed, higher quality. */
    const val COLLECTION_HERO_WIDTH = 1280
    const val COLLECTION_HERO_HEIGHT = 720

    const val CAST_WIDTH = 200
    const val CAST_HEIGHT = 267

    /** Cast avatar on detail — 120dp @ 2× density, square crop. */
    const val CAST_AVATAR_PX = 240

    const val CRITIC_LOGO_WIDTH = 360
    const val CRITIC_LOGO_HEIGHT = 180

    /** Watch hidden gems wordmark — splash + header lockup. */
    const val WATCH_HIDDEN_GEMS_WIDTH = 160
    const val WATCH_HIDDEN_GEMS_HEIGHT = 64

    /** Bunny edge enhancement — same as web CDN_IMAGE_QUALITY / CDN_IMAGE_SHARPEN. */
    const val CDN_QUALITY = "85"
    const val CDN_SHARPEN = "true"

    /** Prefer AVIF on API 31+; Bunny falls back when Accept is set on the client. */
    private fun bunnyFormat(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "avif" else "webp"

    fun forCard(url: String): String = withDimensions(url, CARD_WIDTH, CARD_HEIGHT)

    fun forPortraitCard(url: String): String =
        withDimensions(url, PORTRAIT_CARD_WIDTH, PORTRAIT_CARD_HEIGHT, quality = "90")

    fun forShowcase(url: String): String = withDimensions(url, SHOWCASE_WIDTH, SHOWCASE_HEIGHT)

    fun forDetail(url: String): String = withDimensions(url, DETAIL_WIDTH, DETAIL_HEIGHT)

    fun forCollectionHero(url: String): String =
        withDimensions(url, COLLECTION_HERO_WIDTH, COLLECTION_HERO_HEIGHT, quality = "90")

    fun forCast(url: String): String = withDimensions(url, CAST_WIDTH, CAST_HEIGHT)

    fun forCastAvatar(url: String): String =
        withDimensions(url, CAST_AVATAR_PX, CAST_AVATAR_PX, quality = "100")

    fun forCriticLogo(url: String): String =
        withDimensions(url, CRITIC_LOGO_WIDTH, CRITIC_LOGO_HEIGHT, quality = "100")

    fun forWatchHiddenGems(url: String): String =
        withDimensions(url, WATCH_HIDDEN_GEMS_WIDTH, WATCH_HIDDEN_GEMS_HEIGHT)

    fun withDimensions(url: String?, width: Int, height: Int, quality: String = CDN_QUALITY): String {
        if (url.isNullOrBlank()) return ""
        val trimmed = normalizeDoubleSlash(url.trim())
        if (!trimmed.startsWith("http", ignoreCase = true) && !trimmed.startsWith("//")) {
            return trimmed
        }
        if (width <= 0 || height <= 0) return trimmed

        return try {
            val parseable = if (trimmed.startsWith("//")) "https:$trimmed" else trimmed
            val uri = Uri.parse(parseable)
            val host = uri.host.orEmpty().lowercase()

            // Only Bunny Optimizer understands width/height/format/quality/sharpen.
            if (!isBunnyHost(host)) return trimmed
            if (hasSignedParams(uri)) return trimmed

            val builder = uri.buildUpon().clearQuery()
            for (name in uri.queryParameterNames) {
                if (name.equals("width", ignoreCase = true) ||
                    name.equals("height", ignoreCase = true) ||
                    name.equals("h", ignoreCase = true) ||
                    name.equals("w", ignoreCase = true) ||
                    name.equals("format", ignoreCase = true) ||
                    name.equals("quality", ignoreCase = true) ||
                    name.equals("sharpen", ignoreCase = true)
                ) {
                    continue
                }
                uri.getQueryParameters(name).forEach { value ->
                    builder.appendQueryParameter(name, value)
                }
            }

            // Match web optimizer keys (width/height only — no extra h/w).
            builder.appendQueryParameter("width", width.toString())
            builder.appendQueryParameter("height", height.toString())
            builder.appendQueryParameter("format", bunnyFormat())
            builder.appendQueryParameter("quality", quality)
            builder.appendQueryParameter("sharpen", CDN_SHARPEN)

            val optimized = builder.build().toString()
            if (trimmed.startsWith("//")) optimized.removePrefix("https:") else optimized
        } catch (_: Exception) {
            trimmed
        }
    }

    /** Fix `b-cdn.net//assetlibrary/...` paths that some assets ship with. */
    private fun normalizeDoubleSlash(url: String): String {
        val schemeSplit = url.indexOf("://")
        if (schemeSplit < 0) return url.replace("//", "/")
        val head = url.substring(0, schemeSplit + 3)
        val rest = url.substring(schemeSplit + 3).replace("//", "/")
        return head + rest
    }

    private fun isBunnyHost(host: String): Boolean =
        host.endsWith(".b-cdn.net") || host == "b-cdn.net"

    private fun hasSignedParams(uri: Uri): Boolean {
        for (name in uri.queryParameterNames) {
            val key = name.lowercase()
            if (key.startsWith("x-amz-") ||
                key == "signature" ||
                key == "token" ||
                key == "bcdn_token" ||
                key == "policy" ||
                key == "expires" ||
                key == "x-goog-signature"
            ) {
                return true
            }
        }
        return false
    }
}
