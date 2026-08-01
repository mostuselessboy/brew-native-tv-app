package com.google.jetstream.presentation.screens.movies

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import com.google.jetstream.R

/** Secondary hero actions — icon-only circles in one focus traversal row. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieDetailSecondaryActions(
    showTrailer: Boolean,
    showSubtitles: Boolean,
    showShare: Boolean = true,
    onTrailerClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    isBookmarked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showTrailer) {
            SecondaryActionButton(
                onClick = onTrailerClick,
                contentDescription = "Trailer",
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mdi_clapperboard),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        if (showSubtitles) {
            SecondaryActionButton(
                onClick = onSubtitlesClick,
                contentDescription = "Subtitles",
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mdi_subtitles),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        SecondaryActionButton(
            onClick = onBookmarkClick,
            contentDescription = if (isBookmarked) "Saved" else "Save",
        ) {
            Icon(
                painter = painterResource(
                    if (isBookmarked) R.drawable.ic_fa_bookmark else R.drawable.ic_lucide_bookmark,
                ),
                contentDescription = null,
                tint = if (isBookmarked) MovieDetailTokens.AccentYellow else Color.White,
                modifier = Modifier.size(17.dp),
            )
        }
        if (showShare) {
            SecondaryActionButton(
                onClick = onShareClick,
                contentDescription = "Share",
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_share),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SecondaryActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
        label = "secondaryScale",
    )
    val buttonSize = MovieDetailTokens.SecondaryActionSize

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(buttonSize)
            .onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = scale),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, MovieDetailTokens.SecondaryActionBorder)),
            focusedBorder = Border(
                border = BorderStroke(1.5.dp, Color.White),
                shape = CircleShape,
            ),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = Color.White.copy(alpha = 0.22f),
                elevation = 12.dp,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Black,
            focusedContainerColor = Color.Black.copy(alpha = 0.85f),
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            icon()
        }
    }
}
