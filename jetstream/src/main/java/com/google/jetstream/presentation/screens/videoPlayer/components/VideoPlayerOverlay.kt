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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.jetstream.presentation.theme.JetStreamTheme

/**
 * Handles the visibility and animation of the controls.
 */
@Composable
fun VideoPlayerOverlay(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    isControlsVisible: Boolean = true,
    showControls: () -> Unit = {},
    onDismissControls: () -> Unit = {},
    centerButton: @Composable () -> Unit = {},
    centerControls: @Composable () -> Unit = {},
    topControls: @Composable (() -> Unit)? = null,
    subtitles: @Composable () -> Unit = {},
    controls: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (!isControlsVisible) return@onPreviewKeyEvent false
                val native = event.nativeKeyEvent
                if (native.keyCode == KeyEvent.KEYCODE_BACK &&
                    native.action == KeyEvent.ACTION_DOWN
                ) {
                    onDismissControls()
                    true
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(isControlsVisible, Modifier, fadeIn(), fadeOut()) {
            CinematicBackground(Modifier.fillMaxSize())
        }

        AnimatedVisibility(
            isControlsVisible,
            Modifier.align(Alignment.Center),
            fadeIn(),
            fadeOut(),
        ) {
            centerControls()
        }

        Column {
            Box(
                Modifier.weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                subtitles()
            }

            AnimatedVisibility(
                isControlsVisible,
                Modifier,
                slideInVertically { it },
                slideOutVertically { it }
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 56.dp)
                        .padding(bottom = 24.dp, top = 4.dp)
                ) {
                    controls()
                }
            }
        }
        if (topControls != null) {
            AnimatedVisibility(
                isControlsVisible,
                Modifier.align(Alignment.TopCenter),
                fadeIn(),
                fadeOut(),
            ) {
                Box(modifier = Modifier.padding(horizontal = 56.dp, vertical = 32.dp)) {
                    topControls()
                }
            }
        }
        centerButton()
    }
}

@Composable
fun CinematicBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.Black.copy(alpha = 0.94f),
                    0.18f to Color.Black.copy(alpha = 0.42f),
                    0.42f to Color.Transparent,
                    0.58f to Color.Transparent,
                    0.82f to Color.Black.copy(alpha = 0.48f),
                    1f to Color.Black.copy(alpha = 0.97f),
                ),
            ),
        ),
    )
}

@Preview(device = "id:tv_4k")
@Composable
private fun VideoPlayerOverlayPreview() {
    JetStreamTheme {
        Box(Modifier.fillMaxSize()) {
            VideoPlayerOverlay(
                modifier = Modifier.align(Alignment.BottomCenter),
                isPlaying = true,
                subtitles = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Red)
                    )
                },
                controls = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Blue)
                    )
                },
                centerButton = {
                    Box(
                        Modifier
                            .size(88.dp)
                            .background(Color.Green)
                    )
                }
            )
        }
    }
}
