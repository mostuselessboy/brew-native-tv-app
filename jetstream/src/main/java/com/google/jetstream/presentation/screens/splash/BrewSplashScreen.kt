package com.google.jetstream.presentation.screens.splash

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.google.jetstream.R

private val SplashBlack = Color(0xFF000000)
private val SpinnerTrack = Color(0xFF2A2A2A)
private val SpinnerArc = Color.White

/**
 * Animated splash — logo scales/fades in once, then a lightweight rotating
 * arc spinner runs continuously. Optimized to avoid allocations in DrawScope.
 */
@Composable
fun BrewSplashScreen(modifier: Modifier = Modifier) {
    // One-shot logo entrance.
    val logoScale = remember { Animatable(0.85f) }
    val logoAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(260, easing = LinearEasing))
    }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(420, easing = EaseOutBack))
    }

    // Continuous spinner rotation — pure graphicsLayer rotation, cheap.
    val rotation by rememberInfiniteTransition(label = "splashSpin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
        ),
        label = "spinnerRotation",
    )

    val density = LocalDensity.current
    val strokeWidthPx = remember(density) { with(density) { 3.dp.toPx() } }
    val stroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBlack),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.brew_logo),
                contentDescription = "Brew",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(56.dp)
                    .width(88.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    },
            )

            Canvas(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = rotation },
            ) {
                drawArc(
                    color = SpinnerTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                )
                drawArc(
                    color = SpinnerArc,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = stroke,
                )
            }
        }
    }
}
