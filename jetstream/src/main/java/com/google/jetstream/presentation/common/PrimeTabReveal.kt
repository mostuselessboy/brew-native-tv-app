package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

private const val RevealHoldMs = 48L
private const val RevealFadeMs = 200
private val RevealEasing = FastOutSlowInEasing

/**
 * Prime-style tab switch: brief black hold, then content fades in (no slide — avoids white flash).
 */
@Composable
fun PrimeTabReveal(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(active) {
        if (!active) {
            revealed = false
            return@LaunchedEffect
        }
        revealed = false
        delay(RevealHoldMs)
        revealed = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusProperties { canFocus = active && revealed },
    ) {
        if (active) {
            if (!revealed) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                )
            }
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(RevealFadeMs, easing = RevealEasing)),
                exit = fadeOut(tween(100, easing = RevealEasing)),
            ) {
                Box(Modifier.fillMaxSize()) {
                    content()
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .focusProperties { canFocus = false },
            ) {
                content()
            }
        }
    }
}
