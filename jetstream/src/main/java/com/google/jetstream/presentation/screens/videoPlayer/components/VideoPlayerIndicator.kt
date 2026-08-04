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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.jetstream.data.util.SeekSpritePreview

private val BrewAccentYellow = Color(0xFFFFC15E)

private val ThumbSizeSeeking = 18.dp
private val ThumbSizeFocused = 14.dp
private val ThumbSizeDefault = 1.dp
private val TrackHeightSeeking = 8.dp
private val TrackHeightFocused = 5.dp
private val TrackHeightDefault = 3.dp
private val BarRowHeightSeeking = 32.dp
private val BarRowHeightFocused = 24.dp
private val BarRowHeightDefault = 20.dp
private val PreviewGapAboveThumb = 8.dp
private val TrackRowSlotHeight = 48.dp // matches VideoPlayerSeeker's PlayPauseSlotSize

private val PreviewTimestampBlockHeight = 37.dp

@Composable
fun VideoPlayerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = BrewAccentYellow,
    isFocused: Boolean = false,
    isSeeking: Boolean = false,
    showThumbPreview: Boolean = false,
    previewTimeSeconds: Double = 0.0,
    durationSeconds: Double = 0.0,
    bunnyVideoId: String? = null,
    bunnyCdnZone: String? = null,
) {
    val isEmphasized = isFocused || isSeeking
    val barColor by animateColorAsState(
        targetValue = if (isEmphasized) BrewAccentYellow else Color.White,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "progressBarColor",
    )
    val thumbColor = Color.White
    val inactiveTrackAlpha by animateFloatAsState(
        targetValue = if (isEmphasized) 0.24f else 0.28f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "inactiveTrackAlpha",
    )
    val trackHeight by animateDpAsState(
        targetValue = when {
            isSeeking -> TrackHeightSeeking
            isFocused -> TrackHeightFocused
            else -> TrackHeightDefault
        },
        label = "trackHeight",
    )
    val thumbSize by animateDpAsState(
        targetValue = when {
            isSeeking -> ThumbSizeSeeking
            isFocused -> ThumbSizeFocused
            else -> ThumbSizeDefault
        },
        label = "thumbSize",
    )
    val barRowHeight by animateDpAsState(
        targetValue = when {
            isSeeking -> BarRowHeightSeeking
            isFocused -> BarRowHeightFocused
            else -> BarRowHeightDefault
        },
        label = "barRowHeight",
    )

    val previewMeta = remember(previewTimeSeconds, durationSeconds, bunnyVideoId, bunnyCdnZone, isSeeking) {
        val videoId = SeekSpritePreview.normalizeVideoId(bunnyVideoId)
        val cdnZone = bunnyCdnZone?.trim().orEmpty()
        if (videoId.isBlank() || durationSeconds <= 0) {
            null
        } else {
            SeekSpritePreview.framePreview(
                timeSeconds = previewTimeSeconds,
                durationSeconds = durationSeconds,
                videoId = videoId,
                cdnZone = cdnZone,
                maxPreviewWidthPx = if (isSeeking) 300 else SeekSpritePreview.DEFAULT_PREVIEW_WIDTH_PX,
                maxPreviewHeightPx = if (isSeeking) 220 else SeekSpritePreview.DEFAULT_PREVIEW_HEIGHT_PX,
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        val density = LocalDensity.current
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val clamped = progress.coerceIn(0f, 1f)
        val thumbCenterPx = trackWidthPx * clamped
        val thumbCenterDp = with(density) { thumbCenterPx.toDp() }
        val thumbOffsetX = thumbCenterDp - thumbSize / 2
        val previewWidthDp = previewMeta?.let {
            with(density) { (it.previewWidth + 12f).toDp() }
        }
        val previewCardHeightDp = previewMeta?.let {
            with(density) { it.previewHeight.toDp() } + PreviewTimestampBlockHeight
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            if (showThumbPreview && previewMeta != null && previewWidthDp != null && previewCardHeightDp != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(previewCardHeightDp + PreviewGapAboveThumb),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Box(
                        modifier = Modifier.offset(x = thumbCenterDp - previewWidthDp / 2),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        SeekThumbPreview(
                            preview = previewMeta,
                            timeSeconds = previewTimeSeconds,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TrackRowSlotHeight),
                contentAlignment = Alignment.CenterStart,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight)
                        .align(Alignment.Center),
                    onDraw = {
                        val yOffset = size.height / 2f
                        drawLine(
                            color = barColor.copy(alpha = inactiveTrackAlpha),
                            start = Offset(0f, yOffset),
                            end = Offset(size.width, yOffset),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = barColor,
                            start = Offset(0f, yOffset),
                            end = Offset(size.width * clamped, yOffset),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round,
                        )
                    },
                )

                Box(
                    modifier = Modifier
                        .offset(x = thumbOffsetX)
                        .size(thumbSize)
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .background(thumbColor, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.VideoPlayerControllerIndicator(
    progress: Float,
    onPlayPauseToggle: () -> Unit,
    onShowControls: () -> Unit = {},
    onDismissControls: () -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    progressColor: Color? = null,
    modifier: Modifier = Modifier,
    showThumbPreview: Boolean = false,
    isSeeking: Boolean = false,
    previewTimeSeconds: Double = 0.0,
    durationSeconds: Double = 0.0,
    bunnyVideoId: String? = null,
    bunnyCdnZone: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    androidx.compose.runtime.LaunchedEffect(isFocused) {
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

    VideoPlayerProgressBar(
        progress = progress,
        modifier = modifier
            .weight(1f)
            .focusable(interactionSource = interactionSource)
            .then(enterKeyModifier),
        progressColor = progressColor ?: BrewAccentYellow,
        isFocused = isFocused,
        isSeeking = isSeeking,
        showThumbPreview = showThumbPreview && (isFocused || isSeeking),
        previewTimeSeconds = previewTimeSeconds,
        durationSeconds = durationSeconds,
        bunnyVideoId = bunnyVideoId,
        bunnyCdnZone = bunnyCdnZone,
    )
}
