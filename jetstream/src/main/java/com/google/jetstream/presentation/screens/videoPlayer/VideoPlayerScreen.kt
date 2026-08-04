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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.playback.PlaybackIntent
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.videoPlayer.components.EndScreenOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.NetflixSeekBurst
import com.google.jetstream.presentation.screens.videoPlayer.components.NetflixSeekDirection
import com.google.jetstream.presentation.screens.videoPlayer.components.PlayerEnterOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerCenterControls
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerControls
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerState
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberNetflixSeekBurstState
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberPlayer
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberVideoPlayerState
import com.google.jetstream.presentation.utils.handleDPadKeyEvents

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
            Loading(modifier = Modifier.fillMaxSize())
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
) {
    val context = LocalContext.current
    val exoPlayer = rememberPlayer(context)

    val videoPlayerState = rememberVideoPlayerState(
        hideSeconds = 3,
    )

    val streamUrl = playback?.hlsUrl?.takeIf { it.isNotBlank() } ?: movieDetails.videoUri
    val initialTimeMs = playback?.initialTimeMs ?: 0L
    var showEnterOverlay by remember(movieDetails.id) { mutableStateOf(true) }

    LaunchedEffect(exoPlayer, streamUrl, initialTimeMs) {
        exoPlayer.clearMediaItems()
        if (streamUrl.isNotBlank()) {
            exoPlayer.setMediaItem(movieDetails.intoMediaItem(streamUrl))
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

    BackHandler(onBack = onBackPressed)

    val burstState = rememberNetflixSeekBurstState()

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    showEnterOverlay = false
                }
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnded()
                }
                if (playbackState == Player.STATE_IDLE && exoPlayer.playerError != null) {
                    showEnterOverlay = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
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

    Box(
        Modifier
            .dPadEvents(
                exoPlayer = exoPlayer,
                videoPlayerState = videoPlayerState,
                burstState = burstState,
            )
            .focusable()
    ) {
        PlayerSurface(
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier.resizeWithContentScale(
                contentScale = ContentScale.Fit,
                sourceSizeDp = null
            )
        )

        val focusRequester = remember { FocusRequester() }
        VideoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            focusRequester = focusRequester,
            isPlaying = exoPlayer.isPlaying,
            isControlsVisible = videoPlayerState.isControlsVisible,
            centerButton = {
                if (!videoPlayerState.isControlsVisible) {
                    NetflixSeekBurst(state = burstState)
                }
            },
            centerControls = {
                VideoPlayerCenterControls(
                    player = exoPlayer,
                    burstState = burstState,
                    onShowControls = { videoPlayerState.showControls(exoPlayer.isPlaying) },
                    onSeekBack = exoPlayer::seekBack,
                    onSeekForward = exoPlayer::seekForward,
                )
            },
            subtitles = { /* TODO Implement subtitles */ },
            showControls = videoPlayerState::showControls,
            controls = {
                VideoPlayerControls(
                    player = exoPlayer,
                    movieDetails = movieDetails,
                    focusRequester = focusRequester,
                    onShowControls = { videoPlayerState.showControls(exoPlayer.isPlaying) },
                )
            }
        )

        if (enterOverlayAlpha > 0f && movieDetails.posterUri.isNotBlank()) {
            PlayerEnterOverlay(
                visible = showEnterOverlay,
                posterUri = movieDetails.posterUri,
                title = movieDetails.name,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(enterOverlayAlpha),
            )
        }

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

private fun Modifier.dPadEvents(
    exoPlayer: ExoPlayer,
    videoPlayerState: VideoPlayerState,
    burstState: com.google.jetstream.presentation.screens.videoPlayer.components.NetflixSeekBurstState,
): Modifier = this.handleDPadKeyEvents(
    onLeft = {
        if (!videoPlayerState.isControlsVisible) {
            exoPlayer.seekBack()
            burstState.trigger(NetflixSeekDirection.Back)
        }
    },
    onRight = {
        if (!videoPlayerState.isControlsVisible) {
            exoPlayer.seekForward()
            burstState.trigger(NetflixSeekDirection.Forward)
        }
    },
    onUp = { videoPlayerState.showControls() },
    onDown = { videoPlayerState.showControls() },
    onEnter = {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        videoPlayerState.showControls(isPlaying = exoPlayer.isPlaying)
    }
)

private fun MovieDetails.intoMediaItem(streamUrl: String): MediaItem {
    val builder = MediaItem.Builder()
        .setUri(streamUrl)
    if (streamUrl.contains(".m3u8", ignoreCase = true)) {
        builder.setMimeType(MimeTypes.APPLICATION_M3U8)
    }
    return builder
        .setSubtitleConfigurations(
            if (subtitleUri == null) {
                emptyList()
            } else {
                listOf(
                    MediaItem.SubtitleConfiguration
                        .Builder(Uri.parse(subtitleUri))
                        .setMimeType("application/vtt")
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                )
            }
        ).build()
}
