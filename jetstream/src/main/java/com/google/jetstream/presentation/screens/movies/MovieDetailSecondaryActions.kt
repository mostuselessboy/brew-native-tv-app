package com.google.jetstream.presentation.screens.movies

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import com.google.jetstream.R

/** Secondary hero actions — icon-only circles, no hover labels. */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
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
    firstFocusRequester: FocusRequester? = null,
    leftFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    val defaultFirstFocusRequester = remember { FocusRequester() }
    val firstItem = firstFocusRequester ?: defaultFirstFocusRequester

    Row(
        modifier = modifier
            .focusGroup()
            .graphicsLayer { clip = false },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var isFirst = true
        if (showTrailer) {
            SecondaryActionButton(
                onClick = onTrailerClick,
                modifier = firstItemModifier(
                    isFirst = isFirst,
                    firstFocusRequester = firstItem,
                    leftFocusRequester = leftFocusRequester,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mdi_clapperboard),
                    contentDescription = "Trailer",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            isFirst = false
        }
        if (showSubtitles) {
            SecondaryActionButton(
                onClick = onSubtitlesClick,
                modifier = firstItemModifier(
                    isFirst = isFirst,
                    firstFocusRequester = firstItem,
                    leftFocusRequester = leftFocusRequester,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mdi_subtitles),
                    contentDescription = "Subtitles",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            isFirst = false
        }
        SecondaryActionButton(
            onClick = onBookmarkClick,
            modifier = firstItemModifier(
                isFirst = isFirst,
                firstFocusRequester = firstItem,
                leftFocusRequester = leftFocusRequester,
            ),
        ) {
            Icon(
                painter = painterResource(
                    if (isBookmarked) R.drawable.ic_fa_bookmark else R.drawable.ic_lucide_bookmark,
                ),
                contentDescription = if (isBookmarked) "Saved" else "Save",
                tint = if (isBookmarked) MovieDetailTokens.AccentYellow else Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
        isFirst = false
        if (showShare) {
            SecondaryActionButton(
                onClick = onShareClick,
                modifier = Modifier,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_share),
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun firstItemModifier(
    isFirst: Boolean,
    firstFocusRequester: FocusRequester?,
    leftFocusRequester: FocusRequester?,
): Modifier {
    if (!isFirst) return Modifier
    return Modifier
        .then(if (firstFocusRequester != null) Modifier.focusRequester(firstFocusRequester) else Modifier)
        .then(
            if (leftFocusRequester != null) {
                Modifier.focusProperties { left = leftFocusRequester }
            } else {
                Modifier
            },
        )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
        label = "secondaryScale",
    )
    val buttonSize = MovieDetailTokens.SecondaryActionSize

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(buttonSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                clip = false
            }
            .onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, MovieDetailTokens.SecondaryActionBorder)),
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.85f)),
                shape = CircleShape,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Black,
            focusedContainerColor = Color.Black.copy(alpha = 0.85f),
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(buttonSize),
        ) {
            icon()
        }
    }
}
