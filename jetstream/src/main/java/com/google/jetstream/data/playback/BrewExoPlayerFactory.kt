package com.google.jetstream.data.playback

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.jetstream.data.util.BrewTrailerUrl
import com.google.jetstream.data.util.BunnyStream

@UnstableApi
object BrewExoPlayerFactory {

    fun buildPlayer(context: Context, accessToken: String? = null): ExoPlayer {
        val headers = mutableMapOf(
            "Referer" to BrewTrailerUrl.REFERER,
            "Origin" to BrewTrailerUrl.ORIGIN,
        )
        if (!accessToken.isNullOrBlank()) {
            headers["Authorization"] = "Bearer $accessToken"
            headers["st-auth-mode"] = "header"
        }
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val drmSessionManagerProvider = DefaultDrmSessionManagerProvider().apply {
            setDrmHttpDataSourceFactory(httpFactory)
        }
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .setDrmSessionManagerProvider(drmSessionManagerProvider)

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setSeekForwardIncrementMs(10_000)
            .setSeekBackIncrementMs(10_000)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
    }

    fun buildMediaItem(
        streamUrl: String,
        playback: PlaybackIntent?,
        subtitleUri: String? = null,
    ): MediaItem {
        val builder = MediaItem.Builder().setUri(streamUrl)
        val licenseUri = playback?.let { resolveWidevineLicenseUrl(it) }.orEmpty()
        val useDrm = playback?.isDrm == true || licenseUri.isNotBlank()
        if (useDrm && licenseUri.isNotBlank()) {
            builder.setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(licenseUri)
                    .setForceDefaultLicenseUri(true)
                    .build(),
            )
        }
        val subs = subtitleUri?.takeIf { it.isNotBlank() }
        if (subs != null) {
            builder.setSubtitleConfigurations(
                listOf(
                    MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(subs))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build(),
                ),
            )
        }
        return builder.build()
    }

    fun resolveWidevineLicenseUrl(playback: PlaybackIntent): String {
        playback.licenseServerUrl?.takeIf { it.isNotBlank() }?.let { return it }
        val libraryId = playback.bunnyLibraryId?.takeIf { it.isNotBlank() }
        val videoId = playback.bunnyVideoId?.takeIf { it.isNotBlank() }
        if (libraryId != null && videoId != null) {
            return BunnyStream.widevineLicenseUrl(libraryId, videoId)
        }
        return ""
    }
}
