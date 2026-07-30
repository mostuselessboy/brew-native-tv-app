package com.google.jetstream.presentation.screens.videoPlayer.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.jetstream.data.util.BrewTrailerUrl

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberPlayer(context: Context): ExoPlayer {
    val player = remember {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(
                mapOf(
                    "Referer" to BrewTrailerUrl.REFERER,
                    "Origin" to BrewTrailerUrl.ORIGIN,
                )
            )
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)

        ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(10_000)
            .setSeekBackIncrementMs(10_000)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    return player
}
