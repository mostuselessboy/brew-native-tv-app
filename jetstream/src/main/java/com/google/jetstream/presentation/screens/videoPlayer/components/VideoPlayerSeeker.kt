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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.google.jetstream.R
import com.google.jetstream.presentation.utils.handleHoldSeekKeyEvents

private val BrewAccentYellow = Color(0xFFFFC15E)
private const val POSITION_TICK_MS = 50L

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSeeker(
    player: Player,
    focusRequester: FocusRequester,
    holdSeekState: VideoPlayerHoldSeekState,
    modifier: Modifier = Modifier,
    progressColor: Color = BrewAccentYellow,
    durationSeconds: Double = 0.0,
    bunnyVideoId: String? = null,
    bunnyCdnZone: String? = null,
    feedbackState: VideoPlayerFeedbackState? = null,
    onShowControls: (isPlaying: Boolean) -> Unit = {},
    onDismissControls: () -> Unit = {},
    onStartHoldSeek: (NetflixSeekDirection) -> Unit = {},
    onTapSeek: (NetflixSeekDirection) -> Unit = {},
    onCommitHoldSeek: () -> Unit = {},
    onCancelHoldSeek: () -> Unit = {},
    onBumpSeekSpeed: (NetflixSeekDirection) -> Unit = {},
    onInteraction: () -> Unit = {},
) {
    val contentDuration = player.contentDuration.milliseconds
    val durationMs = contentDuration.inWholeMilliseconds

    var currentPositionMs by remember(player) {
        mutableLongStateOf(player.currentPosition.coerceAtLeast(0L))
    }
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
        currentPositionMs = player.currentPosition.coerceAtLeast(0L)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, holdSeekState.isActive) {
        while (isActive) {
            if (!holdSeekState.isActive) {
                currentPositionMs = player.currentPosition.coerceAtLeast(0L)
            }
            delay(POSITION_TICK_MS)
        }
    }

    val displayPositionMs = when {
        holdSeekState.isActive -> holdSeekState.positionMs
        else -> currentPositionMs
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

    val showThumbPreview = holdSeekState.isActive

    val previewTimeSeconds = displayPositionMs / 1000.0

    val timeLabel = formatTimeRemaining(
        currentMs = displayPositionMs,
        durationMs = durationMs,
    )
    val currentLabel = formatTime(ms = displayPositionMs)

    val seekKeysModifier = Modifier.handleHoldSeekKeyEvents(
        holdSeekState = holdSeekState,
        enabled = { durationMs > 0L },
        onStartHold = { direction ->
            onShowControls(player.isPlaying)
            onStartHoldSeek(direction)
        },
        onTapSeek = { direction ->
            onShowControls(player.isPlaying)
            onTapSeek(direction)
        },
        onCommit = onCommitHoldSeek,
        onCancel = onCancelHoldSeek,
        onBumpSpeed = onBumpSeekSpeed,
        onSeekKeyDown = { direction -> feedbackState?.onSeekKeyDown(direction) },
        onSeekKeyUp = { feedbackState?.onSeekKeyReleased() },
        onInteraction = onInteraction,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VideoPlayerPlayPauseIcon(
                isPlaying = isPlaying,
                highlighted = isSeekBarFocused || holdSeekState.isActive,
                controlsChromeVisible = true,
            )

            VideoPlayerControllerText(text = currentLabel, isRemaining = false)
        }

        VideoPlayerControllerIndicator(
            modifier = Modifier
                .focusRequester(focusRequester)
                .then(seekKeysModifier),
            progress = animatedProgress,
            onPlayPauseToggle = {
                if (holdSeekState.isActive) {
                    onCommitHoldSeek()
                    isPlaying = player.playWhenReady
                } else if (player.isPlaying) {
                    player.pause()
                    onShowControls(false)
                } else {
                    player.playWhenReady = true
                    player.play()
                    onInteraction()
                }
            },
            onShowControls = { onShowControls(player.isPlaying) },
            onDismissControls = onDismissControls,
            onFocusChanged = { isSeekBarFocused = it },
            progressColor = progressColor,
            showThumbPreview = showThumbPreview,
            isSeeking = holdSeekState.isActive,
            previewTimeSeconds = previewTimeSeconds,
            durationSeconds = durationSeconds,
            bunnyVideoId = bunnyVideoId,
            bunnyCdnZone = bunnyCdnZone,
        )

        VideoPlayerControllerText(text = timeLabel, isRemaining = true)
    }
}

private val PlayPauseSlotSize = 48.dp

@Composable
fun VideoPlayerPlayPauseIcon(
    isPlaying: Boolean,
    highlighted: Boolean = false,
    controlsChromeVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val iconSize = if (highlighted) 22.dp else 20.dp
    val scale by animateFloatAsState(
        targetValue = if (highlighted) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "playPauseScale",
    )
    val iconTint = when {
        !controlsChromeVisible -> Color.Black
        highlighted -> Color.Black
        else -> Color.White
    }

    if (controlsChromeVisible) {
        val backgroundColor = if (highlighted) {
            Color.White
        } else {
            Color.White.copy(alpha = 0.05f)
        }
        Box(
            modifier = modifier
                .size(PlayPauseSlotSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                Image(
                    painter = painterResource(
                        if (isPlaying) R.drawable.ic_brew_pause else R.drawable.ic_brew_play,
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(iconTint),
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    } else {
        Image(
            painter = painterResource(
                if (isPlaying) R.drawable.ic_brew_pause else R.drawable.ic_brew_play,
            ),
            contentDescription = if (isPlaying) "Pause" else "Play",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.Black),
            modifier = modifier.size(40.dp),
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
