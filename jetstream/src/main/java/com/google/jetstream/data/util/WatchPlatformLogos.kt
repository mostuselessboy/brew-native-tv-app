package com.google.jetstream.data.util

/** Brew CDN watch-platform badges — mirrors vod-frontend Footer + mobile drawer. */
object WatchPlatformLogos {

    data class Entry(
        val url: String,
        val widthDp: Float,
        val heightDp: Float,
    )

    val footerRow: List<Entry> = listOf(
        Entry(
            url = "https://createstir.b-cdn.net/stir-static/google-tv-logo.png",
            widthDp = 72f,
            heightDp = 16f,
        ),
        Entry(
            url = "https://createstir.b-cdn.net/stir-static/amazon-fire-tv-logo.png",
            widthDp = 56f,
            heightDp = 22f,
        ),
        Entry(
            url = "https://createstir.b-cdn.net/stir-static/playstore.png",
            widthDp = 56f,
            heightDp = 18f,
        ),
        Entry(
            url = "https://createstir.b-cdn.net/stir-static/appstore.png",
            widthDp = 56f,
            heightDp = 18f,
        ),
        Entry(
            url = "https://createstir.b-cdn.net/stir-static/apple-tv-logo.png",
            widthDp = 32f,
            heightDp = 16f,
        ),
    )
}
