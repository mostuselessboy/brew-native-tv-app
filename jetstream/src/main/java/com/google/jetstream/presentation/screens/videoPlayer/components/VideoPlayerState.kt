/*
 * Copyright 2023 Google LLC
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

import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
class VideoPlayerState(
    @IntRange(from = 0)
    private val hideSeconds: Int,
    private val scope: CoroutineScope,
) {
    var isControlsVisible by mutableStateOf(true)
        private set

    private var hideJob: Job? = null
    private var interactionHoldCount = 0

    fun hideControls() {
        hideJob?.cancel()
        isControlsVisible = false
    }

    /** Keep chrome visible while seeking, settings open, or a key is held. */
    fun holdControlsVisible() {
        interactionHoldCount++
        hideJob?.cancel()
        isControlsVisible = true
    }

    fun releaseControlsHold() {
        interactionHoldCount = (interactionHoldCount - 1).coerceAtLeast(0)
        if (interactionHoldCount == 0 && isControlsVisible) {
            scheduleHide(hideSeconds)
        }
    }

    fun showControls(isPlaying: Boolean = true) {
        isControlsVisible = true
        if (interactionHoldCount > 0) {
            hideJob?.cancel()
            return
        }
        val seconds = if (isPlaying) hideSeconds else Int.MAX_VALUE
        scheduleHide(seconds)
    }

    /** Reset the auto-hide timer after the last control interaction. */
    fun notifyInteraction(isPlaying: Boolean = true) {
        if (!isControlsVisible || interactionHoldCount > 0) return
        val seconds = if (isPlaying) hideSeconds else Int.MAX_VALUE
        scheduleHide(seconds)
    }

    private fun scheduleHide(seconds: Int) {
        hideJob?.cancel()
        if (seconds == Int.MAX_VALUE) return
        hideJob = scope.launch {
            delay(seconds * 1000L)
            if (interactionHoldCount == 0) {
                isControlsVisible = false
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberVideoPlayerState(
    @IntRange(from = 0) hideSeconds: Int = 2,
): VideoPlayerState {
    val scope = rememberCoroutineScope()
    return remember(hideSeconds, scope) {
        VideoPlayerState(
            hideSeconds = hideSeconds,
            scope = scope,
        )
    }
}
