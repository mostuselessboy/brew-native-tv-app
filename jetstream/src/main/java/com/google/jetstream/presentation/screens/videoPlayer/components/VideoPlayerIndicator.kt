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

import android.view.KeyEvent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme

@Composable
fun RowScope.VideoPlayerControllerIndicator(
    progress: Float,
    onPlayPauseToggle: () -> Unit,
    onShowControls: () -> Unit = {},
    onDismissControls: () -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    progressColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color = progressColor ?: if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp.times((if (isFocused) 2.5f else 1f)),
        label = "indicatorHeight",
    )

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
        if (isFocused) {
            onShowControls()
        }
    }

    val enterKeyModifier = Modifier.onPreviewKeyEvent { event ->
        val native = event.nativeKeyEvent
        when {
            native.action == KeyEvent.ACTION_DOWN &&
                native.keyCode == KeyEvent.KEYCODE_BACK -> {
                onDismissControls()
                true
            }
            native.action == KeyEvent.ACTION_UP &&
                (native.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    native.keyCode == KeyEvent.KEYCODE_ENTER ||
                    native.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) -> {
                onShowControls()
                onPlayPauseToggle()
                true
            }
            else -> false
        }
    }

    Canvas(
        modifier = modifier
            .weight(1f)
            .height(animatedIndicatorHeight)
            .padding(horizontal = 4.dp)
            .then(enterKeyModifier)
            .focusable(interactionSource = interactionSource),
        onDraw = {
            val yOffset = size.height.div(2)
            val clamped = progress.coerceIn(0f, 1f)
            drawLine(
                color = color.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width, y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width.times(clamped), y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round,
            )
        },
    )
}
