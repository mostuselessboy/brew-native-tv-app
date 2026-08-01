package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.data.util.SeekSpritePreview
import com.google.jetstream.presentation.theme.BrewTitle

private val PreviewBg = Color(0xFF1A1A1A)

@Composable
fun VideoPlayerSeekPreview(
    timeSeconds: Double,
    durationSeconds: Double,
    bunnyVideoId: String?,
    bunnyCdnZone: String?,
    modifier: Modifier = Modifier,
) {
    val videoId = SeekSpritePreview.normalizeVideoId(bunnyVideoId)
    val cdnZone = bunnyCdnZone?.trim().orEmpty()
    if (videoId.isBlank() || durationSeconds <= 0) return

    val preview = remember(timeSeconds, durationSeconds, videoId, cdnZone) {
        SeekSpritePreview.framePreview(
            timeSeconds = timeSeconds,
            durationSeconds = durationSeconds,
            videoId = videoId,
            cdnZone = cdnZone,
        )
    } ?: return

    val context = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(preview.spriteIndex, videoId, cdnZone) {
        val nextUrl = SeekSpritePreview.spriteUrl(cdnZone, videoId, preview.spriteIndex + 1)
        context.imageLoader.enqueue(
            ImageRequest.Builder(context).data(nextUrl).build(),
        )
    }

    val previewWidthDp = with(density) { preview.previewWidth.toDp() }
    val previewHeightDp = with(density) { preview.previewHeight.toDp() }
    val spriteWidthDp = with(density) {
        (preview.frameWidth * preview.scale * SeekSpritePreview.SPRITE_COLUMNS).toDp()
    }
    val spriteHeightDp = with(density) {
        (preview.frameHeight * preview.scale * SeekSpritePreview.SPRITE_ROWS).toDp()
    }
    val offsetXDp = with(density) { preview.offsetX.toDp() }
    val offsetYDp = with(density) { preview.offsetY.toDp() }

    Box(
        modifier = modifier
            .width(previewWidthDp + 8.dp)
            .background(PreviewBg.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(previewWidthDp)
                .height(previewHeightDp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(preview.spriteUrl)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(spriteWidthDp)
                    .height(spriteHeightDp)
                    .graphicsLayer {
                        translationX = offsetXDp.toPx()
                        translationY = offsetYDp.toPx()
                    },
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.35f),
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    ),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    text = formatSeekTimestamp(timeSeconds),
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
    }
}

private fun formatSeekTimestamp(seconds: Double): String {
    val total = seconds.coerceAtLeast(0.0).toInt()
    val mins = total / 60
    val secs = total % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
