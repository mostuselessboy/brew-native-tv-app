package com.google.jetstream.data.util

import com.google.jetstream.R

/** Local watch-platform badges — copied from vod-mono-frontend `watchPlatformAssets.ts`. */
object WatchPlatformLogos {

    data class Entry(
        val drawableRes: Int,
        /** Display height on TV footer row. */
        val heightDp: Float,
        /** Width ÷ height — same ratio as mobile-viewer @1x PNGs. */
        val aspectRatio: Float,
    ) {
        val widthDp: Float = heightDp * aspectRatio
    }

    val footerRow: List<Entry> = listOf(
        Entry(
            drawableRes = R.drawable.watch_platform_google_tv,
            heightDp = 16f,
            aspectRatio = 150f / 22f,
        ),
        Entry(
            drawableRes = R.drawable.watch_platform_fire_tv,
            heightDp = 22f,
            aspectRatio = 72f / 48f,
        ),
        Entry(
            drawableRes = R.drawable.watch_platform_google_play,
            heightDp = 18f,
            aspectRatio = 150f / 28f,
        ),
        Entry(
            drawableRes = R.drawable.watch_platform_app_store,
            heightDp = 18f,
            aspectRatio = 132f / 28f,
        ),
        Entry(
            drawableRes = R.drawable.watch_platform_apple_tv,
            heightDp = 16f,
            aspectRatio = 72f / 22f,
        ),
    )
}
