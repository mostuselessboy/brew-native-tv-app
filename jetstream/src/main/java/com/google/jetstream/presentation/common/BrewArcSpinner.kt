package com.google.jetstream.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Lightweight rotating arc spinner — same visual as the splash screen loader. */
@Composable
fun BrewArcSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    strokeWidth: Dp = 3.dp,
    trackColor: Color = Color(0xFF2A2A2A),
    arcColor: Color = Color.White,
) {
    val rotation by rememberInfiniteTransition(label = "brewArcSpin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
        ),
        label = "brewArcRotation",
    )

    val density = LocalDensity.current
    val strokeWidthPx = remember(density, strokeWidth) { with(density) { strokeWidth.toPx() } }
    val stroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation },
    ) {
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke,
        )
        drawArc(
            color = arcColor,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            style = stroke,
        )
    }
}
