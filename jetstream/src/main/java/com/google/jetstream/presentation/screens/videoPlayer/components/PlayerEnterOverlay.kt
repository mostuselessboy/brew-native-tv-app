package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Mini-viewer enter handoff — port of mobile-viewer `CardExpandOverlay`.
 * Poster scales up from card-sized rect to full screen while playback loads.
 */
@Composable
fun PlayerEnterOverlay(
    visible: Boolean,
    posterUri: String,
    title: String,
    modifier: Modifier = Modifier,
    onExpansionComplete: () -> Unit = {},
) {
    if (!visible) return

    val context = LocalContext.current
    val progress = remember { Animatable(0f) }
    var showContent by remember { mutableStateOf(false) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(posterUri) {
        progress.snapTo(0f)
        contentAlpha.snapTo(0f)
        showContent = false
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        )
        showContent = true
        onExpansionComplete()
        contentAlpha.animateTo(1f, animationSpec = tween(250))
    }

    val scale = 0.28f + (0.72f * progress.value)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(posterUri)
                .crossfade(true)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
                .background(Color.Black),
        )

        if (showContent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 52.sp,
                    letterSpacing = (-2).sp,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Curating your next watch",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 50.dp),
                )
            }
        }
    }
}
