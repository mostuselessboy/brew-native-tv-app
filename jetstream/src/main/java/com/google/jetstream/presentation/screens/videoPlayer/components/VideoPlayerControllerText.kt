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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

private val TimeLabelWidth = 58.dp
private val TimeLabelRowHeight = 48.dp

@Composable
fun VideoPlayerControllerText(
    text: String,
    isRemaining: Boolean,
) {
    Box(
        modifier = Modifier
            .height(TimeLabelRowHeight)
            .width(TimeLabelWidth),
        contentAlignment = if (isRemaining) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = com.google.jetstream.presentation.theme.BrewTitle,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = if (isRemaining) TextAlign.Start else TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(TimeLabelWidth),
        )
    }
}
