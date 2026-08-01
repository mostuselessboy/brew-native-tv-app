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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.media3.common.Player
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.data.entities.MovieDetails

/**
 * Netflix-style bottom chrome — title + seek bar only (no media-action sidebar).
 * Port of mobile-viewer `BottomControls` + `TopControls` title row.
 */
@Composable
fun VideoPlayerControls(
    player: Player,
    movieDetails: MovieDetails,
    playback: PlaybackIntent?,
    focusRequester: FocusRequester,
    onShowControls: () -> Unit = {},
) {
    val metaLine = buildString {
        movieDetails.releaseYear.takeIf { it.isNotBlank() }?.let { append(it) }
        if (isNotEmpty() && movieDetails.duration.isNotBlank() && movieDetails.duration != "—") {
            append("  •  ")
        }
        if (movieDetails.duration.isNotBlank() && movieDetails.duration != "—") {
            append(movieDetails.duration)
        }
    }

    VideoPlayerMainFrame(
        mediaTitle = {
            VideoPlayerMediaTitle(
                title = playback?.title?.takeIf { it.isNotBlank() } ?: movieDetails.name,
                secondaryText = metaLine,
                tertiaryText = "",
                type = if (playback?.isTrailer == true) {
                    VideoPlayerMediaTitleType.TRAILER
                } else {
                    VideoPlayerMediaTitleType.DEFAULT
                },
            )
        },
        mediaActions = null,
        seeker = {
            VideoPlayerSeeker(
                player = player,
                focusRequester = focusRequester,
                bunnyVideoId = playback?.bunnyVideoId,
                bunnyCdnZone = playback?.bunnyCdnZone,
                onShowControls = onShowControls,
            )
        },
        more = null
    )
}
