package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

data class PlayerSettingsPillLabels(
    val subtitles: String,
    val quality: String,
    val speed: String,
)

@OptIn(UnstableApi::class)
@Composable
fun rememberPlayerSettingsPillLabels(player: Player): PlayerSettingsPillLabels {
    var refreshToken by remember(player) { mutableIntStateOf(0) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                refreshToken++
            }

            override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                refreshToken++
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    return remember(refreshToken) {
        PlayerSettingsPillLabels(
            subtitles = VideoPlayerTrackHelper.subtitlePillLabel(player),
            quality = VideoPlayerTrackHelper.qualityPillLabel(player),
            speed = VideoPlayerTrackHelper.speedPillLabel(player),
        )
    }
}
