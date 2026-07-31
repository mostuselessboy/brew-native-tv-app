package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import com.google.jetstream.data.util.StringConstants

/**
 * Netflix-style center chrome — ±10s burst buttons flanking play/pause.
 * Port of mobile-viewer `CenterControls.tsx`.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerCenterControls(
    player: Player,
    burstState: NetflixSeekBurstState,
    modifier: Modifier = Modifier,
    onShowControls: () -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
) {
    val playPauseState = rememberPlayPauseButtonState(player)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VideoPlayerControlsIcon(
            icon = Icons.Default.Replay10,
            isPlaying = player.isPlaying,
            contentDescription = StringConstants.Composable.VideoPlayerControlPlayPauseButton,
            onShowControls = onShowControls,
            onClick = {
                onSeekBack()
                burstState.trigger(NetflixSeekDirection.Back)
            },
        )
        VideoPlayerControlsIcon(
            modifier = Modifier
                .size(80.dp)
                .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            icon = if (playPauseState.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
            isPlaying = player.isPlaying,
            contentDescription = StringConstants.Composable.VideoPlayerControlPlayPauseButton,
            onShowControls = onShowControls,
            onClick = playPauseState::onClick,
        )
        VideoPlayerControlsIcon(
            icon = Icons.Default.Forward10,
            isPlaying = player.isPlaying,
            contentDescription = StringConstants.Composable.VideoPlayerControlPlayPauseButton,
            onShowControls = onShowControls,
            onClick = {
                onSeekForward()
                burstState.trigger(NetflixSeekDirection.Forward)
            },
        )
    }
}
