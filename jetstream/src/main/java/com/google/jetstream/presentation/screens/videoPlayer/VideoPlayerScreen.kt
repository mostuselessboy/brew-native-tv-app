/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.videoPlayer

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.painterResource
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.R
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.playback.BrewExoPlayerFactory
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.presentation.common.BrewArcSpinner
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.screens.videoPlayer.components.EndScreenOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.NetflixSeekDirection
import com.google.jetstream.presentation.screens.videoPlayer.components.TransientPlayPauseIcon
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerChromelessFeedback
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerFeedbackState
import com.google.jetstream.presentation.screens.videoPlayer.components.CHROMELESS_SEEK_SECONDS
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerControls
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerSettingsButton
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerSettingsDialog
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerSettingsOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerState
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberPlayerSettingsPillLabels
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberVideoPlayerFeedbackState
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerHoldSeekState
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerSeekOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.WarmSeekSpritesEffect
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberPlayer
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberVideoPlayerState
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import com.google.jetstream.presentation.utils.handleHoldSeekKeyEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

object VideoPlayerScreen {
    const val MovieIdBundleKey = "movieId"
}

@Composable
fun VideoPlayerScreen(
    onBackPressed: () -> Unit,
    onPlayAnotherMovie: (String) -> Unit = {},
    videoPlayerScreenViewModel: VideoPlayerScreenViewModel = hiltViewModel()
) {
    val uiState by videoPlayerScreenViewModel.uiState.collectAsStateWithLifecycle()
    val endScreenState by videoPlayerScreenViewModel.endScreenState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        videoPlayerScreenViewModel.switchToMovie.collect { slug ->
            onPlayAnotherMovie(slug)
        }
    }

    when (val s = uiState) {
        is VideoPlayerScreenUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                BrewArcSpinner(size = 40.dp, strokeWidth = 3.5.dp)
            }
        }

        is VideoPlayerScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is VideoPlayerScreenUiState.Done -> {
            VideoPlayerScreenContent(
                movieDetails = s.movieDetails,
                playback = s.playback,
                endScreenState = endScreenState,
                onBackPressed = onBackPressed,
                onPlaybackEnded = { videoPlayerScreenViewModel.onPlaybackEnded(s.movieDetails) },
                onEndScreenPick = videoPlayerScreenViewModel::playEndScreenRecommendation,
                onDismissEndScreen = videoPlayerScreenViewModel::dismissEndScreen,
                accessToken = videoPlayerScreenViewModel.accessToken,
                onSyncProgress = videoPlayerScreenViewModel::syncVideoProgress,
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreenContent(
    movieDetails: MovieDetails,
    playback: PlaybackIntent?,
    endScreenState: EndScreenUiState = EndScreenUiState.Hidden,
    onBackPressed: () -> Unit,
    onPlaybackEnded: () -> Unit = {},
    onEndScreenPick: (com.google.jetstream.data.entities.EndScreenRecommendation) -> Unit = {},
    onDismissEndScreen: () -> Unit = {},
    accessToken: String? = null,
    onSyncProgress: (
        vodAssetId: Int,
        positionSeconds: Double,
        durationSeconds: Double,
        isCheckpoint: Boolean,
    ) -> Unit = { _, _, _, _ -> },
) {
    val context = LocalContext.current
    val exoPlayer = rememberPlayer(context, accessToken)

    val videoPlayerState = rememberVideoPlayerState(
        hideSeconds = 3,
    )

    val streamUrl = playback?.hlsUrl?.takeIf { it.isNotBlank() } ?: movieDetails.videoUri
    val initialTimeMs = playback?.initialTimeMs ?: 0L
    var showEnterOverlay by remember(movieDetails.id) { mutableStateOf(false) }
    var isBuffering by remember(movieDetails.id) { mutableStateOf(true) }
    var settingsDialog by remember { mutableStateOf(VideoPlayerSettingsDialog.None) }
    var isPlaying by remember(exoPlayer) { mutableStateOf(exoPlayer.isPlaying) }
    val rootFocusRequester = remember { FocusRequester() }
    val seekFocusRequester = remember { FocusRequester() }

    val hidePlayerChrome: () -> Unit = {
        videoPlayerState.hideControls()
        runCatching { rootFocusRequester.requestFocus() }
    }

    val dismissSettingsDialog: () -> Unit = {
        settingsDialog = VideoPlayerSettingsDialog.None
        videoPlayerState.showControls(isPlaying)
        runCatching { seekFocusRequester.requestFocus() }
    }

    DisposableEffect(exoPlayer) {
        val playingListener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(playingListener)
        isPlaying = exoPlayer.isPlaying
        onDispose { exoPlayer.removeListener(playingListener) }
    }

    LaunchedEffect(exoPlayer, streamUrl, playback, initialTimeMs) {
        exoPlayer.clearMediaItems()
        if (streamUrl.isNotBlank()) {
            exoPlayer.setMediaItem(
                BrewExoPlayerFactory.buildMediaItem(
                    streamUrl = streamUrl,
                    playback = playback,
                    subtitleUri = movieDetails.subtitleUri,
                ),
            )
            exoPlayer.prepare()
            if (initialTimeMs > 0L) {
                exoPlayer.seekTo(initialTimeMs)
            }
            exoPlayer.playWhenReady = true
        } else {
            showEnterOverlay = false
        }
    }

    LaunchedEffect(streamUrl) {
        if (streamUrl.isBlank()) return@LaunchedEffect
        kotlinx.coroutines.delay(10_000)
        showEnterOverlay = false
    }

    val feedbackState = rememberVideoPlayerFeedbackState()
    val holdSeekState = remember { VideoPlayerHoldSeekState() }
    val settingsPills = rememberPlayerSettingsPillLabels(exoPlayer)
    val bunnyVideoId = playback?.bunnyVideoId
    val bunnyCdnZone = playback?.bunnyCdnZone
    var durationSeconds by remember { mutableDoubleStateOf(0.0) }

    val startHoldSeek: (NetflixSeekDirection) -> Unit = { direction ->
        feedbackState.clear()
        videoPlayerState.showControls(isPlaying = isPlaying)
        holdSeekState.begin(direction, exoPlayer)
        isPlaying = false
    }
    val commitHoldSeek: () -> Unit = {
        holdSeekState.commit(exoPlayer)
        isPlaying = exoPlayer.isPlaying
    }
    val cancelHoldSeek: () -> Unit = {
        holdSeekState.cancel(exoPlayer)
        isPlaying = exoPlayer.isPlaying
    }
    val bumpSeekSpeed: (NetflixSeekDirection) -> Unit = { direction ->
        holdSeekState.bumpSpeed(direction)
    }
    val tapSeekChromeless: (NetflixSeekDirection) -> Unit = { direction ->
        tapSeekBy(exoPlayer, direction, CHROMELESS_SEEK_SECONDS * 1000L)
        feedbackState.triggerSeek(direction)
    }
    val tapSeekBar: (NetflixSeekDirection) -> Unit = { direction ->
        tapSeekBy(exoPlayer, direction, 10_000L)
    }

    LaunchedEffect(holdSeekState.isActive, holdSeekState.speed) {
        while (isActive && holdSeekState.isActive) {
            val durationMs = exoPlayer.duration.coerceAtLeast(0L)
            val intervalMs = when (holdSeekState.speed) {
                5 -> 50L
                2 -> 75L
                else -> 100L
            }
            delay(intervalMs)
            if (!holdSeekState.tick(durationMs)) {
                holdSeekState.syncPlayer(exoPlayer)
                break
            }
            holdSeekState.syncPlayer(exoPlayer)
        }
    }

    WarmSeekSpritesEffect(
        bunnyVideoId = bunnyVideoId,
        bunnyCdnZone = bunnyCdnZone,
        durationSeconds = durationSeconds,
    )

    DisposableEffect(playback?.vodAssetId, playback?.isTrailer) {
        onDispose {
            val vodAssetId = playback?.vodAssetId ?: return@onDispose
            if (playback.isTrailer) return@onDispose
            val positionSec = exoPlayer.currentPosition.coerceAtLeast(0L) / 1000.0
            val durationSec = exoPlayer.duration.coerceAtLeast(0L) / 1000.0
            onSyncProgress(vodAssetId, positionSec, durationSec, true)
        }
    }

    LaunchedEffect(playback?.vodAssetId, playback?.isTrailer, isPlaying) {
        val vodAssetId = playback?.vodAssetId ?: return@LaunchedEffect
        if (playback.isTrailer) return@LaunchedEffect
        while (isActive) {
            delay(5_000)
            if (!isPlaying) continue
            val positionSec = exoPlayer.currentPosition.coerceAtLeast(0L) / 1000.0
            val durationSec = exoPlayer.duration.coerceAtLeast(0L) / 1000.0
            if (durationSec <= 0.0) continue
            onSyncProgress(vodAssetId, positionSec, durationSec, false)
        }
    }

    BackHandler(enabled = settingsDialog == VideoPlayerSettingsDialog.None) {
        when {
            holdSeekState.isActive -> cancelHoldSeek()
            videoPlayerState.isControlsVisible -> hidePlayerChrome()
            else -> onBackPressed()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE
                if (playbackState == Player.STATE_READY) {
                    showEnterOverlay = false
                    durationSeconds = exoPlayer.duration.coerceAtLeast(0L) / 1000.0
                }
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnded()
                }
                if (playbackState == Player.STATE_IDLE && exoPlayer.playerError != null) {
                    showEnterOverlay = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                showEnterOverlay = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    val enterOverlayAlpha by animateFloatAsState(
        targetValue = if (showEnterOverlay) 1f else 0f,
        animationSpec = tween(350),
        label = "enterOverlayFade",
    )

    LaunchedEffect(isBuffering) {
        if (isBuffering) {
            feedbackState.clear()
            if (holdSeekState.isActive) {
                cancelHoldSeek()
            }
        }
    }

    LaunchedEffect(videoPlayerState.isControlsVisible) {
        if (videoPlayerState.isControlsVisible) {
            runCatching { seekFocusRequester.requestFocus() }
        } else {
            runCatching { rootFocusRequester.requestFocus() }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
            .handleHoldSeekKeyEvents(
                holdSeekState = holdSeekState,
                enabled = {
                    !videoPlayerState.isControlsVisible &&
                        !isBuffering &&
                        settingsDialog == VideoPlayerSettingsDialog.None
                },
                onStartHold = startHoldSeek,
                onTapSeek = tapSeekChromeless,
                onCommit = commitHoldSeek,
                onCancel = cancelHoldSeek,
                onBumpSpeed = bumpSeekSpeed,
            )
            .chromelessNavigationKeys(
                videoPlayerState = videoPlayerState,
                holdSeekState = holdSeekState,
                isBuffering = isBuffering,
                onPlayPauseToggle = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.playWhenReady = true
                        exoPlayer.play()
                    }
                    isPlaying = !isPlaying
                    if (videoPlayerState.isControlsVisible) {
                        videoPlayerState.showControls(isPlaying = isPlaying)
                    } else if (!isBuffering) {
                        feedbackState.triggerPlayPause(
                            if (isPlaying) {
                                TransientPlayPauseIcon.Play
                            } else {
                                TransientPlayPauseIcon.Pause
                            },
                        )
                    }
                },
            )
    ) {
        PlayerSurface(
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
            modifier = Modifier
                .fillMaxSize()
                .resizeWithContentScale(
                    contentScale = ContentScale.Fit,
                    sourceSizeDp = null
                )
        )

        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BrewArcSpinner(size = 40.dp, strokeWidth = 3.5.dp)
            }
        }

        if (!videoPlayerState.isControlsVisible && !isBuffering && !holdSeekState.isActive) {
            VideoPlayerChromelessFeedback(
                state = feedbackState,
                modifier = Modifier.fillMaxSize(),
            )
        }

        VideoPlayerSeekOverlay(
            holdSeekState = holdSeekState,
            durationSeconds = durationSeconds,
            bunnyVideoId = bunnyVideoId,
            bunnyCdnZone = bunnyCdnZone,
            modifier = Modifier.fillMaxSize(),
        )

        VideoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            isPlaying = isPlaying,
            isControlsVisible = videoPlayerState.isControlsVisible,
            onDismissControls = hidePlayerChrome,
            centerButton = {},
            centerControls = {},
            topControls = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        VideoPlayerTopButton(
                            iconRes = R.drawable.ic_lucide_arrow_left,
                            contentDescription = "Back",
                            onClick = onBackPressed
                        )
                        VideoPlayerTopButton(
                            iconRes = R.drawable.ic_lucide_rotate_ccw,
                            contentDescription = "Play from beginning",
                            onClick = {
                                exoPlayer.seekTo(0)
                                exoPlayer.play()
                            }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = playback?.title?.takeIf { it.isNotBlank() } ?: movieDetails.name,
                                color = Color.White,
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                letterSpacing = (-0.8).sp,
                            )
                            val subtitleText = buildString {
                                movieDetails.releaseYear.takeIf { it.isNotBlank() }?.let { append(it) }
                                if (isNotEmpty() && movieDetails.duration.isNotBlank() && movieDetails.duration != "—") {
                                    append("  •  ")
                                }
                                if (movieDetails.duration.isNotBlank() && movieDetails.duration != "—") {
                                    append(movieDetails.duration)
                                }
                            }
                            if (subtitleText.isNotBlank()) {
                                Text(
                                    text = subtitleText,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontFamily = BrewTitle,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        Image(
                            painter = painterResource(R.drawable.brew_logo),
                            contentDescription = "Brew",
                            modifier = Modifier.size(52.dp),
                        )
                    }
                }
            },
            subtitles = { /* TODO Implement subtitles */ },
            showControls = videoPlayerState::showControls,
            controls = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.focusGroup(),
                ) {
                    VideoPlayerControls(
                        player = exoPlayer,
                        movieDetails = movieDetails,
                        playback = playback,
                        focusRequester = seekFocusRequester,
                        holdSeekState = holdSeekState,
                        onShowControls = { videoPlayerState.showControls(isPlaying) },
                        onDismissControls = hidePlayerChrome,
                        onStartHoldSeek = startHoldSeek,
                        onTapSeek = tapSeekBar,
                        onCommitHoldSeek = commitHoldSeek,
                        onCancelHoldSeek = cancelHoldSeek,
                        onBumpSeekSpeed = bumpSeekSpeed,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            VideoPlayerSettingsButton(
                                label = "Subtitles (${settingsPills.subtitles})",
                                onClick = { settingsDialog = VideoPlayerSettingsDialog.Subtitles },
                            )
                            VideoPlayerSettingsButton(
                                label = "Quality (${settingsPills.quality})",
                                onClick = { settingsDialog = VideoPlayerSettingsDialog.Quality },
                            )
                            VideoPlayerSettingsButton(
                                label = "Speed (${settingsPills.speed})",
                                onClick = { settingsDialog = VideoPlayerSettingsDialog.Speed },
                            )
                        }
                    }
                }
            }
        )

        VideoPlayerSettingsOverlay(
            dialog = settingsDialog,
            player = exoPlayer,
            onDismiss = dismissSettingsDialog,
            modifier = Modifier.fillMaxSize(),
        )

        if (endScreenState is EndScreenUiState.Ready) {
            EndScreenOverlay(
                picks = (endScreenState as EndScreenUiState.Ready).picks,
                onPickSelected = onEndScreenPick,
                onDismiss = onDismissEndScreen,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun tapSeekBy(
    exoPlayer: ExoPlayer,
    direction: NetflixSeekDirection,
    deltaMs: Long,
) {
    val duration = exoPlayer.duration.coerceAtLeast(0L)
    val target = when (direction) {
        NetflixSeekDirection.Back -> (exoPlayer.currentPosition - deltaMs).coerceAtLeast(0L)
        NetflixSeekDirection.Forward -> (exoPlayer.currentPosition + deltaMs).coerceAtMost(duration)
    }
    exoPlayer.seekTo(target)
}

private fun Modifier.chromelessNavigationKeys(
    videoPlayerState: VideoPlayerState,
    holdSeekState: VideoPlayerHoldSeekState,
    isBuffering: Boolean,
    onPlayPauseToggle: () -> Unit,
): Modifier = this.handleDPadKeyEvents(
    onUp = { videoPlayerState.showControls() },
    onDown = { videoPlayerState.showControls() },
    onEnter = {
        if (!videoPlayerState.isControlsVisible && !isBuffering && !holdSeekState.isActive) {
            onPlayPauseToggle()
        }
    },
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerTopButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.15f),
            focusedContainerColor = Color.White,
            contentColor = Color.White,
            focusedContentColor = Color.Black
        ),
        modifier = modifier.size(40.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
