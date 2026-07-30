/*
 * Copyright 2025 Google LLC
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.theme.BrewTitle
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private val BrewPlayerRed = Color(0xFFE50914)
private val PlayerMuted = Color.White.copy(alpha = 0.72f)

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerControls(
    player: Player,
    movieDetails: MovieDetails,
    focusRequester: FocusRequester,
    onShowControls: () -> Unit = {},
) {
    val state = rememberPlayPauseButtonState(player)
    val contentDuration = player.contentDuration.milliseconds

    var currentPositionMs by remember(player) { mutableLongStateOf(0L) }
    val currentPosition = currentPositionMs.milliseconds

    LaunchedEffect(player) {
        while (true) {
            delay(300)
            currentPositionMs = player.currentPosition
        }
    }

    val progress = if (player.duration > 0) {
        (currentPosition / contentDuration).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Text(
            text = movieDetails.name,
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = (-0.8).sp,
            maxLines = 1,
        )

        val subtitle = buildString {
            val year = movieDetails.releaseDate.take(4).takeIf { it.length == 4 }
            if (!year.isNullOrBlank()) append(year)
            if (movieDetails.duration.isNotBlank() && movieDetails.duration != "—") {
                if (isNotEmpty()) append("  •  ")
                append(movieDetails.duration)
            }
        }
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                color = PlayerMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            VideoPlayerControllerIndicator(
                progress = progress,
                activeColor = BrewPlayerRed,
                onSeek = {
                    player.seekTo(player.duration.times(it).toLong())
                    onShowControls()
                },
                onShowControls = onShowControls,
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VideoPlayerControlsIcon(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .size(44.dp),
                icon = if (state.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
                onClick = state::onClick,
                isPlaying = player.isPlaying,
                contentDescription = StringConstants
                    .Composable
                    .VideoPlayerControlPlayPauseButton,
            )

            Spacer(Modifier.width(12.dp))

            VideoPlayerControllerText(text = formatTime(currentPositionMs))
            Text(
                text = " / ",
                color = PlayerMuted,
                fontSize = 14.sp,
            )
            VideoPlayerControllerText(text = formatTime(player.duration))

            Spacer(Modifier.weight(1f))

            VideoPlayerControlsIcon(
                icon = Icons.Default.ClosedCaption,
                isPlaying = player.isPlaying,
                contentDescription = StringConstants
                    .Composable
                    .VideoPlayerControlClosedCaptionsButton,
                onShowControls = onShowControls,
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}
