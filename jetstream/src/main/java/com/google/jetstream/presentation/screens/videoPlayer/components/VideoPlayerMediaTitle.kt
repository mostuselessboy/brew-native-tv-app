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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.theme.JetStreamTheme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.jetstream.presentation.theme.BrewTitle

enum class VideoPlayerMediaTitleType { AD, LIVE, TRAILER, DEFAULT }

@Composable
fun VideoPlayerMediaTitle(
    title: String,
    secondaryText: String,
    tertiaryText: String,
    modifier: Modifier = Modifier,
    type: VideoPlayerMediaTitleType = VideoPlayerMediaTitleType.DEFAULT
) {
    val subTitle = buildString {
        append(secondaryText)
        if (secondaryText.isNotEmpty() && tertiaryText.isNotEmpty()) append(" • ")
        append(tertiaryText)
    }
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = (-1.2).sp,
            lineHeight = 30.sp,
        )
        if (subTitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Row {
                when (type) {
                    VideoPlayerMediaTitleType.AD -> {
                        Text(
                            text = stringResource(R.string.ad),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black,
                            modifier = Modifier
                                .background(Color(0xFFFBC02D), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .alignByBaseline()
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    VideoPlayerMediaTitleType.LIVE -> {
                        Text(
                            text = stringResource(R.string.live),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier
                                .background(Color(0xFFCC0000), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .alignByBaseline()
                        )

                        Spacer(Modifier.width(8.dp))
                    }

                    VideoPlayerMediaTitleType.TRAILER -> {
                        Text(
                            text = "TRAILER",
                            color = Color.Black,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .background(Color(0xFFFFC15E), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .alignByBaseline()
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    VideoPlayerMediaTitleType.DEFAULT -> {}
                }

                Text(
                    text = subTitle,
                    color = Color.White.copy(alpha = 0.72f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
    }
}

@Preview(name = "TV Series", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewSeries() {
    JetStreamTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "True Detective",
                secondaryText = "S1E5",
                tertiaryText = "The Secret Fate Of All Life",
                type = VideoPlayerMediaTitleType.DEFAULT
            )
        }
    }
}

@Preview(name = "Live", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewLive() {
    JetStreamTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "MacLaren Reveal Their 2022 Car: The MCL36",
                secondaryText = "Formula 1",
                tertiaryText = "54K watching now",
                type = VideoPlayerMediaTitleType.LIVE
            )
        }
    }
}

@Preview(name = "Ads", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewAd() {
    JetStreamTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "Samsung Galaxy Note20 | Ultra 5G",
                secondaryText = "Get the most powerful Note yet",
                tertiaryText = "",
                type = VideoPlayerMediaTitleType.AD
            )
        }
    }
}
