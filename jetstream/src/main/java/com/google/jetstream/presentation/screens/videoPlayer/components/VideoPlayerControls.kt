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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.data.entities.MovieDetails

private val BrewAccentYellow = Color(0xFFFFC15E)

/**
 * Bottom chrome — seek bar + time labels only (title/metadata live in top controls).
 */
@Composable
fun VideoPlayerControls(
    player: Player,
    movieDetails: MovieDetails,
    playback: PlaybackIntent?,
    focusRequester: FocusRequester,
    holdSeekState: VideoPlayerHoldSeekState,
    durationSeconds: Double = 0.0,
    feedbackState: VideoPlayerFeedbackState? = null,
    videoPlayerState: VideoPlayerState? = null,
    isPlaying: Boolean = true,
    onShowControls: (Boolean) -> Unit = {},
    onDismissControls: () -> Unit = {},
    onStartHoldSeek: (NetflixSeekDirection) -> Unit = {},
    onTapSeek: (NetflixSeekDirection) -> Unit = {},
    onCommitHoldSeek: () -> Unit = {},
    onCancelHoldSeek: () -> Unit = {},
    onBumpSeekSpeed: (NetflixSeekDirection) -> Unit = {},
) {
    VideoPlayerSeeker(
        player = player,
        focusRequester = focusRequester,
        holdSeekState = holdSeekState,
        durationSeconds = durationSeconds,
        bunnyVideoId = playback?.bunnyVideoId,
        bunnyCdnZone = playback?.bunnyCdnZone,
        feedbackState = feedbackState,
        onShowControls = { playing ->
            onShowControls(playing)
            videoPlayerState?.notifyInteraction(playing)
        },
        onDismissControls = onDismissControls,
        onStartHoldSeek = onStartHoldSeek,
        onTapSeek = {
            onTapSeek(it)
            videoPlayerState?.notifyInteraction(isPlaying)
        },
        onCommitHoldSeek = onCommitHoldSeek,
        onCancelHoldSeek = onCancelHoldSeek,
        onBumpSeekSpeed = onBumpSeekSpeed,
        onInteraction = { videoPlayerState?.notifyInteraction(isPlaying) },
        progressColor = BrewAccentYellow,
    )
}
