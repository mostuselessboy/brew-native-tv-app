package com.google.jetstream.data.util

import com.google.jetstream.data.remote.BrewVodAssetData
import java.net.URLEncoder

/** Builds signed Bunny HLS URLs — mirrors mobile `buildVodAssetHlsUrl`. */
object VodPlaybackUrl {

    fun buildHlsUrl(asset: BrewVodAssetData): String {
        val videoId = asset.bunnyVideoId.trim()
        val zone = asset.bunnyCdnZone.trim()
        if (videoId.isBlank() || zone.isBlank()) return ""
        val base = BunnyStream.hlsPlaylistUrl(zone, videoId)
        val token = asset.token?.trim().orEmpty()
        return if (token.isBlank()) {
            base
        } else {
            "$base?token=${URLEncoder.encode(token, Charsets.UTF_8.name())}"
        }
    }
}
