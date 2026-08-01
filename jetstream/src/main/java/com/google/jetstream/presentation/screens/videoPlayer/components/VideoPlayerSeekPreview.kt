package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.util.BrewTrailerUrl
import com.google.jetstream.data.util.SeekSpritePreview
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val STRIP_FRAME_COUNT = 7
private const val STRIP_TIME_STEP_SECONDS = 10.0
private val SideFrameMaxWidthPx = 168
private val CenterFrameMaxWidthPx = 280
private val PreviewCardBg = Color(0xFF0A0A0A)
private val PreviewImageBg = Color(0xFF050505)

private fun spriteImageRequest(context: android.content.Context, url: String): ImageRequest =
    ImageRequest.Builder(context)
        .data(url)
        .addHeader("Referer", BrewTrailerUrl.REFERER)
        .addHeader("Origin", BrewTrailerUrl.ORIGIN)
        .crossfade(false)
        .build()

@Composable
fun SeekThumbPreview(
    preview: SeekSpritePreview.FramePreview,
    timeSeconds: Double,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(preview.spriteIndex, preview.spriteUrl) {
        val base = preview.spriteUrl.substringBeforeLast("/seek/")
        (-1..2).forEach { offset ->
            val index = preview.spriteIndex + offset
            if (index < 0) return@forEach
            val url = "$base/seek/_$index.jpg?width=480"
            context.imageLoader.enqueue(spriteImageRequest(context, url))
        }
    }

    val previewWidthDp = with(density) { preview.previewWidth.toDp() }
    val previewHeightDp = with(density) { preview.previewHeight.toDp() }
    val cardShape = RoundedCornerShape(10.dp)
    val imageShape = RoundedCornerShape(6.dp)

    Column(
        modifier = modifier
            .width(previewWidthDp + 12.dp)
            .clip(cardShape)
            .background(PreviewCardBg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), cardShape)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeightDp)
                .clip(imageShape)
                .background(PreviewImageBg),
        ) {
            SpriteFrameImage(
                preview = preview,
                cornerRadius = 6.dp,
            )
        }
        Text(
            text = formatSeekTimestamp(timeSeconds),
            color = Color.White.copy(alpha = 0.92f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
        )
    }
}

@Composable
fun SeekThumbPreview(
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

    SeekThumbPreview(
        preview = preview,
        timeSeconds = timeSeconds,
        modifier = modifier,
    )
}

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
                context.imageLoader.enqueue(spriteImageRequest(context, url))
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
            maxPreviewHeightPx = if (isCenter) 110 else 80,
        )
    } ?: return

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
                preview = preview,
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
        Image(
            painter = painterResource(
                if (direction == NetflixSeekDirection.Forward) {
                    R.drawable.ic_brew_skip_forward
                } else {
                    R.drawable.ic_brew_skip_back
                },
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(22.dp),
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
    preview: SeekSpritePreview.FramePreview,
    cornerRadius: Dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val previewWidth = with(density) { preview.previewWidth.toDp() }
    val previewHeight = with(density) { preview.previewHeight.toDp() }
    val sheetWidth = with(density) { (preview.sheetWidth * preview.scale).toDp() }
    val sheetHeight = with(density) { (preview.sheetHeight * preview.scale).toDp() }
    val offsetX = with(density) { preview.offsetX.toDp() }
    val offsetY = with(density) { preview.offsetY.toDp() }

    Box(
        modifier = Modifier
            .width(previewWidth)
            .height(previewHeight)
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        AsyncImage(
            model = spriteImageRequest(context, preview.spriteUrl),
            contentDescription = null,
            contentScale = ContentScale.None,
            modifier = Modifier
                .width(sheetWidth)
                .height(sheetHeight)
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
