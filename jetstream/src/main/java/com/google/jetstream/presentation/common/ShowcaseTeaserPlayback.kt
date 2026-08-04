package com.google.jetstream.presentation.common

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.jetstream.data.util.BrewTrailerUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val TeaserStartDelayMs = 5000L
private const val TeaserClipDurationMs = 30000L

data class ShowcaseTeaserPlayback(
    val player: ExoPlayer? = null,
    val showVideo: Boolean = false,
    val posterAlpha: Float = 1f,
)

/**
 * Lazy teaser playback for showcase heroes — waits 5s, plays Bunny trailer MP4 with audio,
 * fades poster out, stops after ~30s and restores poster. Player is created only after delay.
 */
@OptIn(UnstableApi::class)
@Composable
fun rememberShowcaseTeaserPlayback(videoUri: String?): ShowcaseTeaserPlayback {
    val context = LocalContext.current
    val playableUrl = remember(videoUri) {
        BrewTrailerUrl.toPlayableMp4(videoUri).takeIf { it.isNotBlank() }
    }

    var showVideo by remember(playableUrl) { mutableStateOf(false) }
    var posterVisible by remember(playableUrl) { mutableStateOf(true) }
    val playerHolder = remember(playableUrl) { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(playableUrl) {
        if (playableUrl == null) return@LaunchedEffect
        delay(TeaserStartDelayMs)

        val player = buildTeaserPlayer(context)
        player.setMediaItem(MediaItem.fromUri(playableUrl))
        player.prepare()
        player.volume = 1f
        player.play()
        playerHolder.value = player
        showVideo = true
        posterVisible = false

        while (isActive) {
            val active = playerHolder.value
            if (active == null) break
            val elapsed = active.currentPosition
            if (elapsed >= TeaserClipDurationMs || active.playbackState == Player.STATE_ENDED) {
                active.pause()
                showVideo = false
                posterVisible = true
                active.release()
                playerHolder.value = null
                break
            }
            delay(250)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerHolder.value?.release()
            playerHolder.value = null
        }
    }

    val posterAlpha by animateFloatAsState(
        targetValue = if (posterVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "showcasePosterAlpha",
    )

    return ShowcaseTeaserPlayback(
        player = if (showVideo) playerHolder.value else null,
        showVideo = showVideo,
        posterAlpha = posterAlpha,
    )
}

@OptIn(UnstableApi::class)
private fun buildTeaserPlayer(context: Context): ExoPlayer {
    val httpFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(
            mapOf(
                "Referer" to BrewTrailerUrl.REFERER,
                "Origin" to BrewTrailerUrl.ORIGIN,
            ),
        )
    val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
        .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .build()
        .apply {
            volume = 1f
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
}
