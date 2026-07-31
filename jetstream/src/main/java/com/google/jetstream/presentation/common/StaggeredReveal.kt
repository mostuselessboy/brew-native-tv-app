package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private val StaggerEasing = FastOutSlowInEasing

/** Staggered slide-in from the right — each step uses a different delay + duration. */
@Composable
fun StaggerRevealStep(
    step: Int,
    resetKey: Any,
    delayMs: Int = 40 + step * 55,
    slideMs: Int = 200 + step * 25,
    fadeMs: Int = 140 + step * 15,
    content: @Composable () -> Unit,
) {
    var visible by remember(resetKey, step) { mutableStateOf(false) }

    LaunchedEffect(resetKey, step) {
        visible = false
        delay(delayMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(fadeMs, easing = StaggerEasing)) +
            slideInHorizontally(
                animationSpec = tween(slideMs, easing = StaggerEasing),
                initialOffsetX = { width -> (width * (0.05f + step * 0.015f)).toInt().coerceAtLeast(20) },
            ),
        exit = fadeOut(tween(80)),
    ) {
        content()
    }
}
