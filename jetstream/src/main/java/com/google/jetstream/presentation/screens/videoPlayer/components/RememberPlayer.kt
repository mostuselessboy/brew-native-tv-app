package com.google.jetstream.presentation.screens.videoPlayer.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.jetstream.data.playback.BrewExoPlayerFactory

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberPlayer(context: Context): ExoPlayer {
    val player = remember {
        BrewExoPlayerFactory.buildPlayer(context).apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    return player
}
