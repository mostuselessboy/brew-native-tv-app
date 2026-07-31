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
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSeeker(
    player: Player,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit = {
        player.seekTo(player.duration.times(it).toLong())
    },
    onShowControls: () -> Unit = {},
) {
    val contentDuration = player.contentDuration.milliseconds

    var currentPositionMs by remember(player) { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            currentPositionMs = player.currentPosition
        }
    }

    val timeLabel = formatTimeRemaining(
        currentMs = currentPositionMs,
        durationMs = contentDuration.inWholeMilliseconds,
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VideoPlayerControllerIndicator(
            modifier = Modifier.focusRequester(focusRequester),
            progress = if (contentDuration.inWholeMilliseconds > 0) {
                currentPositionMs.toFloat() / contentDuration.inWholeMilliseconds
            } else {
                0f
            },
            onSeek = onSeek,
            onShowControls = onShowControls,
            progressColor = Color(0xFFFFC15E),
        )
        VideoPlayerControllerText(text = timeLabel)
    }
}

/** Mirrors mobile-viewer `formatTimeRemaining` — `-MM:SS` while playing. */
private fun formatTimeRemaining(currentMs: Long, durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val remainingSec = ((durationMs - currentMs).coerceAtLeast(0) / 1000).toInt()
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    return "-$minutes:${seconds.toString().padStart(2, '0')}"
}
