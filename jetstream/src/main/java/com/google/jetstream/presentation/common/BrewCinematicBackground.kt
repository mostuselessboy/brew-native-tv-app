package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Pure black + Brew orange aurora — matches vod-frontend / tv-app auth & dice overlays. */
private val BrewSceneBlack = Color(0xFF040406)
private val BrewOrange = Color(0xFFDB4A2B)

@Composable
fun BrewCinematicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize().background(BrewSceneBlack)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to BrewOrange.copy(alpha = 0.14f),
                            0.38f to BrewOrange.copy(alpha = 0.05f),
                            0.72f to Color.Transparent,
                            1f to BrewSceneBlack,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.92f),
                        ),
                        radius = 1400f,
                    ),
                ),
        )
        content()
    }
}
