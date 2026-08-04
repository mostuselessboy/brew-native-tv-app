package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Full-screen seek preview — does not affect control layout. */
@Composable
fun VideoPlayerSeekOverlay(
    holdSeekState: VideoPlayerHoldSeekState,
    durationSeconds: Double,
    bunnyVideoId: String?,
    bunnyCdnZone: String?,
    modifier: Modifier = Modifier,
) {
    if (!holdSeekState.isActive || bunnyVideoId.isNullOrBlank() || durationSeconds <= 0) return

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        VideoPlayerSeekPreviewStrip(
            centerTimeSeconds = holdSeekState.positionMs / 1000.0,
            durationSeconds = durationSeconds,
            bunnyVideoId = bunnyVideoId,
            bunnyCdnZone = bunnyCdnZone,
            seekSpeed = holdSeekState.speed,
            seekDirection = holdSeekState.direction,
            modifier = Modifier.offset(y = (-48).dp),
        )
    }
}
