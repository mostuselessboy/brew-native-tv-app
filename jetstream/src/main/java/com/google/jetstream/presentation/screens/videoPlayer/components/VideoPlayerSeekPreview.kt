package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.util.SeekSpritePreview
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val STRIP_FRAME_COUNT = 7
private const val STRIP_TIME_STEP_SECONDS = 10.0
private val SideFrameMaxWidthPx = 168
private val CenterFrameMaxWidthPx = 280

@Composable
fun WarmSeekSpritesEffect(
    bunnyVideoId: String?,
    bunnyCdnZone: String?,
    durationSeconds: Double,
) {
    val context = LocalContext.current
    val videoId = SeekSpritePreview.normalizeVideoId(bunnyVideoId)
    val cdnZone = bunnyCdnZone?.trim().orEmpty()

    LaunchedEffect(videoId, cdnZone, durationSeconds) {
        if (videoId.isBlank() || durationSeconds <= 0) return@LaunchedEffect
        val urls = SeekSpritePreview.spriteUrlsToWarm(
            durationSeconds = durationSeconds,
            videoId = videoId,
            cdnZone = cdnZone,
            maxSheets = 40,
        )
        withContext(Dispatchers.IO) {
            urls.forEach { url ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(url)
                        .build(),
                )
            }
        }
    }
}

@Composable
fun VideoPlayerSeekPreviewStrip(
    centerTimeSeconds: Double,
    durationSeconds: Double,
    bunnyVideoId: String?,
    bunnyCdnZone: String?,
    modifier: Modifier = Modifier,
    seekSpeed: Int = 1,
    seekDirection: NetflixSeekDirection? = null,
) {
    val videoId = SeekSpritePreview.normalizeVideoId(bunnyVideoId)
    val cdnZone = bunnyCdnZone?.trim().orEmpty()
    if (videoId.isBlank() || durationSeconds <= 0) return

    val frameTimes = remember(centerTimeSeconds, durationSeconds) {
        SeekSpritePreview.stripFrameTimes(
            centerTimeSeconds = centerTimeSeconds,
            durationSeconds = durationSeconds,
            frameCount = STRIP_FRAME_COUNT,
            stepSeconds = STRIP_TIME_STEP_SECONDS,
        )
    }
    val centerIndex = STRIP_FRAME_COUNT / 2

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        frameTimes.forEachIndexed { index, timeSeconds ->
            SeekPreviewFrame(
                timeSeconds = timeSeconds,
                durationSeconds = durationSeconds,
                videoId = videoId,
                cdnZone = cdnZone,
                isCenter = index == centerIndex,
                maxPreviewWidthPx = if (index == centerIndex) {
                    CenterFrameMaxWidthPx
                } else {
                    SideFrameMaxWidthPx
                },
                seekSpeed = if (index == centerIndex) seekSpeed else null,
                seekDirection = if (index == centerIndex) seekDirection else null,
            )
        }
    }
}

@Composable
private fun SeekPreviewFrame(
    timeSeconds: Double,
    durationSeconds: Double,
    videoId: String,
    cdnZone: String,
    isCenter: Boolean,
    maxPreviewWidthPx: Int,
    seekSpeed: Int? = null,
    seekDirection: NetflixSeekDirection? = null,
) {
    val preview = remember(timeSeconds, durationSeconds, videoId, cdnZone, maxPreviewWidthPx) {
        SeekSpritePreview.framePreview(
            timeSeconds = timeSeconds,
            durationSeconds = durationSeconds,
            videoId = videoId,
            cdnZone = cdnZone,
            maxPreviewWidthPx = maxPreviewWidthPx,
            maxPreviewHeightPx = if (isCenter) 168 else 118,
        )
    } ?: return

    val context = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(preview.spriteIndex, videoId, cdnZone) {
        (-2..3).forEach { offset ->
            val index = preview.spriteIndex + offset
            if (index < 0) return@forEach
            val url = SeekSpritePreview.spriteUrl(cdnZone, videoId, index)
            context.imageLoader.enqueue(ImageRequest.Builder(context).data(url).build())
        }
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

    val cornerRadius = if (isCenter) 14.dp else 10.dp
    val borderWidth = if (isCenter) 3.dp else 2.dp
    val borderColor = if (isCenter) Color.White else Color.White.copy(alpha = 0.22f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.Black),
        ) {
            SpriteFrameImage(
                spriteUrl = preview.spriteUrl,
                previewWidth = previewWidthDp,
                previewHeight = previewHeightDp,
                spriteWidth = spriteWidthDp,
                spriteHeight = spriteHeightDp,
                offsetX = offsetXDp,
                offsetY = offsetYDp,
                cornerRadius = cornerRadius,
            )

            if (isCenter && seekSpeed != null && seekDirection != null) {
                SeekSpeedBadge(
                    speed = seekSpeed,
                    direction = seekDirection,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                )
            }
        }

        if (isCenter) {
            Text(
                text = formatSeekTimestamp(timeSeconds),
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun SeekSpeedBadge(
    speed: Int,
    direction: NetflixSeekDirection,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(
                if (direction == NetflixSeekDirection.Forward) {
                    R.drawable.ic_lucide_fast_forward
                } else {
                    R.drawable.ic_lucide_rewind
                },
            ),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "${speed}x",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun SpriteFrameImage(
    spriteUrl: String,
    previewWidth: Dp,
    previewHeight: Dp,
    spriteWidth: Dp,
    spriteHeight: Dp,
    offsetX: Dp,
    offsetY: Dp,
    cornerRadius: Dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val spriteWidthPx = with(density) { spriteWidth.roundToPx() }
    val spriteHeightPx = with(density) { spriteHeight.roundToPx() }

    Box(
        modifier = Modifier
            .width(previewWidth)
            .height(previewHeight)
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(spriteUrl)
                .size(spriteWidthPx, spriteHeightPx)
                .crossfade(false)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.None,
            modifier = Modifier
                .width(spriteWidth)
                .height(spriteHeight)
                .offset(x = offsetX, y = offsetY),
        )
    }
}

private fun formatSeekTimestamp(seconds: Double): String {
    val total = seconds.coerceAtLeast(0.0).toInt()
    val mins = total / 60
    val secs = total % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
