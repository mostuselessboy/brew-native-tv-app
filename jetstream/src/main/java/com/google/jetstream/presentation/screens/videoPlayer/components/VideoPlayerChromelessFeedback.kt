package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.jetstream.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

enum class NetflixSeekDirection {
    Forward,
    Back,
}

enum class TransientPlayPauseIcon {
    Play,
    Pause,
}

const val CHROMELESS_SEEK_SECONDS = 10
private const val SEEK_WING_DISMISS_MS = 3_200L

class VideoPlayerFeedbackState(
    private val intervalSeconds: Int = CHROMELESS_SEEK_SECONDS,
) {
    private var _playPauseIcon by mutableStateOf<TransientPlayPauseIcon?>(null)
    val playPauseIcon: TransientPlayPauseIcon? get() = _playPauseIcon

    private var _isSeekKeyHeld by mutableStateOf(false)
    val isSeekKeyHeld: Boolean get() = _isSeekKeyHeld

    private var _wingDirection by mutableStateOf<NetflixSeekDirection?>(null)
    val wingDirection: NetflixSeekDirection? get() = _wingDirection

    private var _accumulatedSeconds by mutableIntStateOf(0)
    val accumulatedSeconds: Int get() = _accumulatedSeconds

    private var _showBarPreview by mutableStateOf(false)
    val showBarPreview: Boolean get() = _showBarPreview

    private var _barPreviewPositionMs by mutableLongStateOf(0L)
    val barPreviewPositionMs: Long get() = _barPreviewPositionMs

    private var _pulseTick by mutableIntStateOf(0)
    val pulseTick: Int get() = _pulseTick

    private val dismissChannel = Channel<Unit>(Channel.CONFLATED)

    @OptIn(FlowPreview::class)
    suspend fun observe() {
        dismissChannel.consumeAsFlow()
            .debounce(900.milliseconds)
            .collect {
                _playPauseIcon = null
            }
    }

    fun triggerPlayPause(icon: TransientPlayPauseIcon) {
        _playPauseIcon = icon
        _wingDirection = null
        _accumulatedSeconds = 0
        _pulseTick++
        dismissChannel.trySend(Unit)
    }

    fun onSeekKeyDown(direction: NetflixSeekDirection) {
        _isSeekKeyHeld = true
        _wingDirection = direction
        _playPauseIcon = null
        _pulseTick++
    }

    fun onSeekKeyReleased() {
        _isSeekKeyHeld = false
        _pulseTick++
    }

    fun completeTapSeek(positionMs: Long, direction: NetflixSeekDirection) {
        if (_wingDirection == direction) {
            if (_accumulatedSeconds > 0) {
                _accumulatedSeconds += intervalSeconds
            } else {
                _accumulatedSeconds = intervalSeconds
            }
        } else {
            _wingDirection = direction
            _accumulatedSeconds = intervalSeconds
        }
        _barPreviewPositionMs = positionMs
        _pulseTick++
    }

    fun clearWings() {
        _wingDirection = null
        _accumulatedSeconds = 0
    }

    fun hideBarPreview() {
        _showBarPreview = false
    }

    fun updateBarPreviewPosition(positionMs: Long) {
        _barPreviewPositionMs = positionMs
        _showBarPreview = true
    }

    fun trigger(direction: NetflixSeekDirection) = onSeekKeyDown(direction)

    fun clear() {
        _playPauseIcon = null
        _wingDirection = null
        _accumulatedSeconds = 0
        _isSeekKeyHeld = false
        _showBarPreview = false
    }
}

@Composable
fun rememberVideoPlayerFeedbackState(
    intervalSeconds: Int = CHROMELESS_SEEK_SECONDS,
): VideoPlayerFeedbackState =
    remember { VideoPlayerFeedbackState(intervalSeconds) }.also { state ->
        LaunchedEffect(state) { state.observe() }
        LaunchedEffect(state.isSeekKeyHeld, state.wingDirection, state.pulseTick) {
            if (state.isSeekKeyHeld || state.wingDirection == null) return@LaunchedEffect
            delay(SEEK_WING_DISMISS_MS)
            state.clearWings()
        }
    }

/** Center play/pause flash + left/right seek wings with rotation. */
@Composable
fun VideoPlayerChromelessFeedback(
    state: VideoPlayerFeedbackState,
    modifier: Modifier = Modifier,
) {
    val playPause = state.playPauseIcon
    val wingDirection = state.wingDirection
    val showWings = wingDirection != null && (state.isSeekKeyHeld || state.accumulatedSeconds > 0)

    if (playPause == null && !showWings) return

    val alpha = remember { Animatable(1f) }

    LaunchedEffect(state.pulseTick, state.isSeekKeyHeld) {
        if (state.pulseTick == 0) return@LaunchedEffect
        if (state.isSeekKeyHeld) {
            alpha.snapTo(1f)
            return@LaunchedEffect
        }
        alpha.snapTo(0.85f)
        alpha.animateTo(1f, tween(140))
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (playPause != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(alpha.value),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        if (playPause == TransientPlayPauseIcon.Play) {
                            R.drawable.ic_brew_play
                        } else {
                            R.drawable.ic_brew_pause
                        },
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(120.dp),
                )
            }
        }

        if (showWings && wingDirection == NetflixSeekDirection.Back) {
            AnimatedSeekWing(
                direction = NetflixSeekDirection.Back,
                pulseTick = state.pulseTick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 72.dp)
                    .alpha(alpha.value),
            )
        }

        if (showWings && wingDirection == NetflixSeekDirection.Forward) {
            AnimatedSeekWing(
                direction = NetflixSeekDirection.Forward,
                pulseTick = state.pulseTick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 72.dp)
                    .alpha(alpha.value),
            )
        }
    }
}

@Composable
private fun AnimatedSeekWing(
    direction: NetflixSeekDirection,
    pulseTick: Int,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(0.92f) }

    LaunchedEffect(pulseTick, direction) {
        rotation.snapTo(0f)
        scale.snapTo(0.92f)
        val targetRotation = if (direction == NetflixSeekDirection.Forward) 10f else -10f
        launch {
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(durationMillis = 220),
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260),
            )
        }
    }

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
        modifier = modifier
            .size(80.dp)
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            },
    )
}

typealias NetflixSeekBurstState = VideoPlayerFeedbackState

@Composable
fun rememberNetflixSeekBurstState(intervalSeconds: Int = CHROMELESS_SEEK_SECONDS): VideoPlayerFeedbackState =
    rememberVideoPlayerFeedbackState(intervalSeconds)
