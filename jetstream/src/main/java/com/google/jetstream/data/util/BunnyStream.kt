package com.google.jetstream.data.util

/**
 * Bunny Stream helpers — mirrors vod-frontend `drmUtils.ts` / trailer HLS build.
 */
object BunnyStream {

    fun hlsPlaylistUrl(cdnZone: String, videoId: String): String =
        "https://$cdnZone.b-cdn.net/$videoId/playlist.m3u8"

    fun widevineLicenseUrl(libraryId: String, videoId: String): String =
        "https://video.bunnycdn.com/WidevineLicense/$libraryId/$videoId"
}
