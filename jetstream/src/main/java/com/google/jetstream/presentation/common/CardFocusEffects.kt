package com.google.jetstream.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface

val BrewCardFocusShape = RoundedCornerShape(9.dp)

/** TV Material default focus scales — no custom spring animation. */
object BrewFocusMotion {
    const val CardScale = 1.10f
    const val ButtonScale = 1.10f
    const val RailScale = 1.10f
}

private val BrewFocusedBorder = Border(
    border = BorderStroke(2.dp, Color.White),
    shape = BrewCardFocusShape,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrewFocusedCardFrame(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = BrewCardFocusShape,
    onFocused: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.onFocusChanged { state ->
            if (state.isFocused) onFocused()
        },
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        scale = ClickableSurfaceDefaults.scale(
            focusedScale = BrewFocusMotion.CardScale,
            pressedScale = 1f,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = BrewFocusedBorder,
            pressedBorder = Border.None,
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = Color.Transparent, elevation = 0.dp),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
        ),
    ) {
        Box(content = content)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrewDetailIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        scale = ClickableSurfaceDefaults.scale(
            focusedScale = BrewFocusMotion.ButtonScale,
            pressedScale = 1f,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border.None,
            pressedBorder = Border.None,
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.14f),
            focusedContainerColor = Color.White,
            pressedContainerColor = Color.White.copy(alpha = 0.9f),
            contentColor = Color.White,
            focusedContentColor = Color.Black,
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = Color.Transparent, elevation = 0.dp),
        ),
    ) {
        Box(content = content)
    }
}
