package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.theme.BrewTitle
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce

enum class NetflixSeekDirection {
    Forward,
    Back,
}

enum class TransientPlayPauseIcon {
    Play,
    Pause,
}

const val CHROMELESS_SEEK_SECONDS = 30

class VideoPlayerFeedbackState(
    private val intervalSeconds: Int = CHROMELESS_SEEK_SECONDS,
) {
    private var _playPauseIcon by mutableStateOf<TransientPlayPauseIcon?>(null)
    val playPauseIcon: TransientPlayPauseIcon? get() = _playPauseIcon

    private var _seekDirection by mutableStateOf<NetflixSeekDirection?>(null)
    val seekDirection: NetflixSeekDirection? get() = _seekDirection

    private var _accumulatedSeconds by mutableIntStateOf(0)
    val accumulatedSeconds: Int get() = _accumulatedSeconds

    private var _pulseTick by mutableIntStateOf(0)
    val pulseTick: Int get() = _pulseTick

    private val dismissChannel = Channel<Unit>(Channel.CONFLATED)

    @OptIn(FlowPreview::class)
    suspend fun observe() {
        dismissChannel.consumeAsFlow()
            .debounce(900.milliseconds)
            .collect {
                _playPauseIcon = null
                _seekDirection = null
                _accumulatedSeconds = 0
            }
    }

    fun triggerPlayPause(icon: TransientPlayPauseIcon) {
        _playPauseIcon = icon
        _seekDirection = null
        _accumulatedSeconds = 0
        _pulseTick++
        dismissChannel.trySend(Unit)
    }

    fun triggerSeek(direction: NetflixSeekDirection) {
        if (_seekDirection == direction) {
            _accumulatedSeconds += intervalSeconds
        } else {
            _seekDirection = direction
            _accumulatedSeconds = intervalSeconds
        }
        _playPauseIcon = null
        _pulseTick++
        dismissChannel.trySend(Unit)
    }

    fun trigger(direction: NetflixSeekDirection) = triggerSeek(direction)

    fun clear() {
        _playPauseIcon = null
        _seekDirection = null
        _accumulatedSeconds = 0
    }
}

@Composable
fun rememberVideoPlayerFeedbackState(
    intervalSeconds: Int = CHROMELESS_SEEK_SECONDS,
): VideoPlayerFeedbackState =
    remember { VideoPlayerFeedbackState(intervalSeconds) }.also {
        LaunchedEffect(it) { it.observe() }
    }

/** Fade overlays when chrome is hidden — center play/pause, wing ±30s tap seek. */
@Composable
fun VideoPlayerChromelessFeedback(
    state: VideoPlayerFeedbackState,
    modifier: Modifier = Modifier,
) {
    val playPause = state.playPauseIcon
    val seekDirection = state.seekDirection
    if (playPause == null && seekDirection == null) return

    val alpha = remember { Animatable(0f) }
    LaunchedEffect(state.pulseTick) {
        if (state.pulseTick == 0) return@LaunchedEffect
        alpha.snapTo(0f)
        alpha.animateTo(1f, tween(160))
        delay(480)
        alpha.animateTo(0f, tween(280))
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (playPause != null) {
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .size(104.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        if (playPause == TransientPlayPauseIcon.Play) {
                            R.drawable.ic_lucide_play_circle
                        } else {
                            R.drawable.ic_lucide_circle_pause
                        },
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp),
                )
            }
        }

        if (seekDirection == NetflixSeekDirection.Back) {
            SeekWingFeedback(
                direction = NetflixSeekDirection.Back,
                seconds = state.accumulatedSeconds,
                alpha = alpha.value,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 80.dp),
            )
        }

        if (seekDirection == NetflixSeekDirection.Forward) {
            SeekWingFeedback(
                direction = NetflixSeekDirection.Forward,
                seconds = state.accumulatedSeconds,
                alpha = alpha.value,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 80.dp),
            )
        }
    }
}

@Composable
private fun SeekWingFeedback(
    direction: NetflixSeekDirection,
    seconds: Int,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
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
                modifier = Modifier.size(44.dp),
            )
        }
        Text(
            text = "${if (direction == NetflixSeekDirection.Forward) "+" else "−"}${seconds}s",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

// Backwards-compatible aliases while call sites migrate.
typealias NetflixSeekBurstState = VideoPlayerFeedbackState

@Composable
fun rememberNetflixSeekBurstState(intervalSeconds: Int = CHROMELESS_SEEK_SECONDS): VideoPlayerFeedbackState =
    rememberVideoPlayerFeedbackState(intervalSeconds)
