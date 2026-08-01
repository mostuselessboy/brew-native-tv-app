package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

data class PlayerSettingsPillLabels(
    val subtitles: String,
    val quality: String,
    val speed: String,
)

@OptIn(UnstableApi::class)
@Composable
fun rememberPlayerSettingsPillLabels(
    player: Player,
    qualityOverride: String? = null,
): PlayerSettingsPillLabels {
    var labels by remember(player) {
        mutableStateOf(buildPillLabels(player, qualityOverride))
    }

    DisposableEffect(player, qualityOverride) {
        fun refresh() {
            labels = buildPillLabels(player, qualityOverride)
        }

        refresh()
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) = refresh()

            override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) =
                refresh()

            override fun onPlaybackParametersChanged(
                playbackParameters: androidx.media3.common.PlaybackParameters,
            ) = refresh()

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) = refresh()
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    return labels
}

@OptIn(UnstableApi::class)
private fun buildPillLabels(
    player: Player,
    qualityOverride: String?,
): PlayerSettingsPillLabels =
    PlayerSettingsPillLabels(
        subtitles = VideoPlayerTrackHelper.subtitlePillLabel(player),
        quality = qualityOverride ?: VideoPlayerTrackHelper.qualityPillLabel(player),
        speed = VideoPlayerTrackHelper.speedPillLabel(player),
    )
