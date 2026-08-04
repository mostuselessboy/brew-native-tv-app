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

package com.google.jetstream.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Typography
import com.google.jetstream.R

/** Brew UI / title face — Suisse Int'l Medium, tight tracking. */
val BrewTitle = FontFamily(
    Font(R.font.brew_title, weight = FontWeight.Medium),
)

/** Cinematic / fancy display face — Instrument Serif. Use with [FontStyle.Italic] for editorial lines. */
val BrewDisplay = FontFamily(
    Font(R.font.instruments_serif_regular, weight = FontWeight.Normal),
)

/** Kept for legacy Lexend usages (logo wordmark fallbacks). */
val LexendExa = BrewTitle

private val TightTitleTracking = (-0.6).sp
private val TightHeadlineTracking = (-0.4).sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = TightTitleTracking,
        fontFamily = BrewDisplay,
        textMotion = TextMotion.Animated
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = TightTitleTracking,
        fontFamily = BrewDisplay,
        textMotion = TextMotion.Animated
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = TightHeadlineTracking,
        fontFamily = BrewDisplay,
        textMotion = TextMotion.Animated
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = TightHeadlineTracking,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = TightHeadlineTracking,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = TightHeadlineTracking,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.3).sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.2).sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.15).sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp,
        fontFamily = BrewTitle,
        textMotion = TextMotion.Animated
    )
)
