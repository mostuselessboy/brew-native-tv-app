package com.google.jetstream.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

private const val ScreenFadeInMs = 220
private const val ScreenFadeOutMs = 180

/** Black-screen fade in — no slide so the window never flashes white. */
fun screenBlackFadeIn() = fadeIn(
    animationSpec = tween(ScreenFadeInMs, easing = FastOutSlowInEasing),
)

/** Black-screen fade out. */
fun screenBlackFadeOut() = fadeOut(
    animationSpec = tween(ScreenFadeOutMs, easing = FastOutSlowInEasing),
)
