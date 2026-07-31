package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce

enum class NetflixSeekDirection {
    Forward,
    Back,
}

/**
 * Port of mobile-viewer `useNetflixSeekBurst` — accumulates ±10s on rapid seeks and
 * shows a sliding +N / −N label beside the seek icon.
 */
class NetflixSeekBurstState(
    private val intervalSeconds: Int = 10,
) {
    private var _direction by mutableStateOf<NetflixSeekDirection?>(null)
    val direction: NetflixSeekDirection? get() = _direction

    private var _accumulatedSeconds by mutableIntStateOf(0)
    val accumulatedSeconds: Int get() = _accumulatedSeconds

    private var _pulseTick by mutableIntStateOf(0)
    val pulseTick: Int get() = _pulseTick

    private val channel = Channel<Unit>(Channel.CONFLATED)

    @OptIn(FlowPreview::class)
    suspend fun observe() {
        channel.consumeAsFlow()
            .debounce(900.milliseconds)
            .collect {
                _accumulatedSeconds = 0
                _direction = null
            }
    }

    fun trigger(direction: NetflixSeekDirection) {
        if (_direction == direction) {
            _accumulatedSeconds += intervalSeconds
        } else {
            _direction = direction
            _accumulatedSeconds = intervalSeconds
        }
        _pulseTick++
        channel.trySend(Unit)
    }
}

@Composable
fun rememberNetflixSeekBurstState(intervalSeconds: Int = 10): NetflixSeekBurstState =
    remember { NetflixSeekBurstState(intervalSeconds) }.also {
        LaunchedEffect(it) { it.observe() }
    }

@Composable
fun NetflixSeekBurst(
    state: NetflixSeekBurstState,
    modifier: Modifier = Modifier,
) {
    val direction = state.direction ?: return
    val icon = when (direction) {
        NetflixSeekDirection.Forward -> Icons.Default.Forward10
        NetflixSeekDirection.Back -> Icons.Default.Replay10
    }
    val prefix = when (direction) {
        NetflixSeekDirection.Forward -> "+"
        NetflixSeekDirection.Back -> "−"
    }
    val isForward = direction == NetflixSeekDirection.Forward

    val bgAlpha by animateFloatAsState(
        targetValue = if (state.pulseTick > 0) 0.3f else 0f,
        animationSpec = tween(160),
        label = "seekBurstBg",
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (state.pulseTick > 0) {
            if (isForward) 18f else -18f
        } else {
            0f
        },
        animationSpec = tween(160),
        label = "seekBurstRotate",
    )
    val labelOffsetX by animateFloatAsState(
        targetValue = if (state.accumulatedSeconds > 0) 52f else 0f,
        animationSpec = tween(420),
        label = "seekBurstLabelSlide",
    )

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .alpha(bgAlpha)
                .background(Color.White, CircleShape),
        )
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .rotate(iconRotation),
            tint = Color.White,
        )
        if (state.accumulatedSeconds > 0) {
            Text(
                text = "$prefix${state.accumulatedSeconds}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(if (isForward) Alignment.CenterEnd else Alignment.CenterStart)
                    .offset(
                        x = if (isForward) {
                            (36 + labelOffsetX).dp
                        } else {
                            (-36 - labelOffsetX).dp
                        },
                    ),
            )
        }
    }
}
