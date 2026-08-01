package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import android.view.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.theme.BrewTitle

enum class VideoPlayerSettingsDialog {
    None,
    Subtitles,
    Quality,
    Speed,
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSettingsOverlay(
    dialog: VideoPlayerSettingsDialog,
    player: Player,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dialog == VideoPlayerSettingsDialog.None) return

    val firstFocus = remember { FocusRequester() }
    val overlayFocus = remember { FocusRequester() }
    LaunchedEffect(dialog) {
        runCatching { overlayFocus.requestFocus() }
        runCatching { firstFocus.requestFocus() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(overlayFocus)
            .focusable()
            .background(Color.Black.copy(alpha = 0.72f))
            .onPreviewKeyEvent { event ->
                val native = event.nativeKeyEvent
                if (native.keyCode == KeyEvent.KEYCODE_BACK) {
                    if (native.action == KeyEvent.ACTION_DOWN) {
                        onDismiss()
                    }
                    true
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 420.dp)
                .background(Color(0xFF1C1C1C), RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when (dialog) {
                        VideoPlayerSettingsDialog.Subtitles -> "Audio & Subtitles"
                        VideoPlayerSettingsDialog.Quality -> "Video Quality"
                        VideoPlayerSettingsDialog.Speed -> "Playback Speed"
                        VideoPlayerSettingsDialog.None -> ""
                    },
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                SettingsCloseButton(onClick = onDismiss)
            }

            when (dialog) {
                VideoPlayerSettingsDialog.Subtitles -> SubtitleDialogContent(
                    player = player,
                    firstFocusRequester = firstFocus,
                    onDismiss = onDismiss,
                )
                VideoPlayerSettingsDialog.Quality -> QualityDialogContent(
                    player = player,
                    firstFocusRequester = firstFocus,
                    onDismiss = onDismiss,
                )
                VideoPlayerSettingsDialog.Speed -> SpeedDialogContent(
                    player = player,
                    firstFocusRequester = firstFocus,
                    onDismiss = onDismiss,
                )
                VideoPlayerSettingsDialog.None -> Unit
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsCloseButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.08f),
            focusedContainerColor = Color.White,
            contentColor = Color.White,
            focusedContentColor = Color.Black,
        ),
        modifier = Modifier.size(32.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_x),
                contentDescription = "Close",
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class)
@Composable
private fun SubtitleDialogContent(
    player: Player,
    firstFocusRequester: FocusRequester,
    onDismiss: () -> Unit,
) {
    var tracks by remember(player) { mutableStateOf(VideoPlayerTrackHelper.readSubtitleTracks(player)) }
    var selectedId by remember(player) {
        mutableStateOf(VideoPlayerTrackHelper.selectedSubtitleId(player, tracks))
    }

    LaunchedEffect(player.currentTracks) {
        tracks = VideoPlayerTrackHelper.readSubtitleTracks(player)
        selectedId = VideoPlayerTrackHelper.selectedSubtitleId(player, tracks)
    }

    SettingsOptionRow(
        label = "Off",
        selected = selectedId == null,
        modifier = Modifier.focusRequester(firstFocusRequester),
        onClick = {
            VideoPlayerTrackHelper.selectSubtitlesOff(player)
            selectedId = null
            onDismiss()
        },
    )

    tracks.forEach { track ->
        SettingsOptionRow(
            label = track.label,
            selected = selectedId == track.id,
            onClick = {
                VideoPlayerTrackHelper.selectSubtitle(player, track)
                selectedId = track.id
                onDismiss()
            },
        )
    }

    if (tracks.isEmpty()) {
        Text(
            text = "No subtitles available",
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = BrewTitle,
            fontSize = 13.sp,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class)
@Composable
private fun QualityDialogContent(
    player: Player,
    firstFocusRequester: FocusRequester,
    onDismiss: () -> Unit,
) {
    var qualities by remember(player) { mutableStateOf(VideoPlayerTrackHelper.readQualityTracks(player)) }
    var selectedHeight by remember(player) {
        mutableStateOf(VideoPlayerTrackHelper.selectedQualityHeight(player, qualities))
    }

    LaunchedEffect(player.currentTracks) {
        qualities = VideoPlayerTrackHelper.readQualityTracks(player)
        selectedHeight = VideoPlayerTrackHelper.selectedQualityHeight(player, qualities)
    }

    SettingsOptionRow(
        label = VideoPlayerTrackHelper.autoQualityLabel(player, qualities),
        selected = selectedHeight == null,
        modifier = Modifier.focusRequester(firstFocusRequester),
        onClick = {
            VideoPlayerTrackHelper.selectQualityAuto(player)
            selectedHeight = null
            onDismiss()
        },
    )

    qualities.forEach { quality ->
        SettingsOptionRow(
            label = quality.label,
            selected = selectedHeight == quality.height,
            onClick = {
                VideoPlayerTrackHelper.selectQuality(player, quality.height)
                selectedHeight = quality.height
                onDismiss()
            },
        )
    }

    if (qualities.isEmpty()) {
        Text(
            text = "Quality options unavailable",
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = BrewTitle,
            fontSize = 13.sp,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SpeedDialogContent(
    player: Player,
    firstFocusRequester: FocusRequester,
    onDismiss: () -> Unit,
) {
    var selectedSpeed by remember(player) { mutableStateOf(player.playbackParameters.speed) }

    VideoPlayerTrackHelper.PLAYBACK_SPEEDS.forEachIndexed { index, option ->
        SettingsOptionRow(
            label = option.label,
            selected = kotlin.math.abs(selectedSpeed - option.speed) < 0.01f,
            modifier = if (index == 0) Modifier.focusRequester(firstFocusRequester) else Modifier,
            onClick = {
                VideoPlayerTrackHelper.setPlaybackSpeed(player, option.speed)
                selectedSpeed = option.speed
                onDismiss()
            },
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) {
                Color.White.copy(alpha = 0.18f)
            } else {
                Color.White.copy(alpha = 0.06f)
            },
            focusedContainerColor = Color.White,
            contentColor = Color.White,
            focusedContentColor = Color.Black,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_check),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerSettingsButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White,
            contentColor = Color.White.copy(alpha = 0.9f),
            focusedContentColor = Color.Black,
        ),
    ) {
        Text(
            text = label,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
        )
    }
}
