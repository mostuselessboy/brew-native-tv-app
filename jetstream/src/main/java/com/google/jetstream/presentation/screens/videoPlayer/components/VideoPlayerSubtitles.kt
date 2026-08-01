package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.text.Cue
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Text
import com.google.jetstream.presentation.theme.BrewTitle

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSubtitles(
    player: Player,
    controlsVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    var cueText by remember(player) { mutableStateOf("") }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onCues(cues: MutableList<Cue>) {
                cueText = cues
                    .mapNotNull { it.text?.toString()?.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    if (cueText.isBlank()) return

    val bottomPadding by animateDpAsState(
        targetValue = if (controlsVisible) 162.dp else 88.dp,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "subtitleBottomPadding",
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = cueText,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(bottom = bottomPadding)
                .padding(horizontal = 24.dp),
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                letterSpacing = (-0.6).sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.72f),
                    offset = Offset(0f, 2f),
                    blurRadius = 8f,
                ),
            ),
        )
    }
}
