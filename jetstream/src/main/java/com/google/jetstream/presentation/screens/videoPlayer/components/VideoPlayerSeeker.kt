/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.google.jetstream.R
import com.google.jetstream.presentation.utils.handleHoldSeekKeyEvents

private val BrewAccentYellow = Color(0xFFFFC15E)
private const val TAP_SEEK_MS = 10_000L
private const val POSITION_TICK_MS = 50L

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSeeker(
    player: Player,
    focusRequester: FocusRequester,
    holdSeekState: VideoPlayerHoldSeekState,
    modifier: Modifier = Modifier,
    progressColor: Color = BrewAccentYellow,
    onShowControls: () -> Unit = {},
    onDismissControls: () -> Unit = {},
    onStartHoldSeek: (NetflixSeekDirection) -> Unit = {},
    onTapSeek: (NetflixSeekDirection) -> Unit = {},
    onCommitHoldSeek: () -> Unit = {},
    onCancelHoldSeek: () -> Unit = {},
    onBumpSeekSpeed: (NetflixSeekDirection) -> Unit = {},
) {
    val contentDuration = player.contentDuration.milliseconds
    val durationMs = contentDuration.inWholeMilliseconds

    var currentPositionMs by remember(player) { mutableLongStateOf(0L) }
    var isSeekBarFocused by remember { mutableStateOf(false) }
    var isPlaying by remember(player) { mutableStateOf(player.isPlaying) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                if (!holdSeekState.isActive) {
                    isPlaying = playing
                }
            }
        }
        player.addListener(listener)
        isPlaying = player.isPlaying
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, holdSeekState.isActive) {
        while (isActive) {
            if (!holdSeekState.isActive && player.isPlaying) {
                currentPositionMs = player.currentPosition
            }
            delay(POSITION_TICK_MS)
        }
    }

    val displayPositionMs = if (holdSeekState.isActive) {
        holdSeekState.positionMs
    } else {
        currentPositionMs
    }
    val targetProgress = if (durationMs > 0) {
        displayPositionMs.toFloat() / durationMs
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = if (holdSeekState.isActive) 0 else 140),
        label = "seekProgress",
    )

    val timeLabel = formatTimeRemaining(
        currentMs = displayPositionMs,
        durationMs = durationMs,
    )
    val currentLabel = formatTime(ms = displayPositionMs)

    val seekKeysModifier = Modifier.handleHoldSeekKeyEvents(
        holdSeekState = holdSeekState,
        enabled = { durationMs > 0L },
        onStartHold = { direction ->
            onShowControls()
            onStartHoldSeek(direction)
        },
        onTapSeek = { direction ->
            onShowControls()
            onTapSeek(direction)
        },
        onCommit = onCommitHoldSeek,
        onCancel = onCancelHoldSeek,
        onBumpSpeed = onBumpSeekSpeed,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        VideoPlayerPlayPauseIcon(
            isPlaying = isPlaying,
            highlighted = isSeekBarFocused,
        )

        VideoPlayerControllerText(text = currentLabel, isRemaining = false)

        VideoPlayerControllerIndicator(
            modifier = Modifier
                .focusRequester(focusRequester)
                .then(seekKeysModifier),
            progress = animatedProgress,
            onPlayPauseToggle = {
                if (holdSeekState.isActive) {
                    onCommitHoldSeek()
                } else if (isPlaying) {
                    player.pause()
                    isPlaying = false
                } else {
                    player.play()
                    isPlaying = true
                }
            },
            onShowControls = onShowControls,
            onDismissControls = onDismissControls,
            onFocusChanged = { isSeekBarFocused = it },
            progressColor = progressColor,
        )

        VideoPlayerControllerText(text = timeLabel, isRemaining = true)
    }
}

@Composable
fun VideoPlayerPlayPauseIcon(
    isPlaying: Boolean,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (highlighted) Color.White else Color.White.copy(alpha = 0.15f)
    val iconTint = if (highlighted) Color.Black else Color.White

    Box(
        modifier = modifier
            .size(48.dp)
            .background(backgroundColor, CircleShape)
            .then(
                if (highlighted) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(
                if (isPlaying) R.drawable.ic_lucide_circle_pause else R.drawable.ic_lucide_play_circle,
            ),
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun formatTimeRemaining(currentMs: Long, durationMs: Long): String {
    if (durationMs <= 0) return "-0:00"
    val remainingSec = ((durationMs - currentMs).coerceAtLeast(0) / 1000).toInt()
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    return "-$minutes:${seconds.toString().padStart(2, '0')}"
}
