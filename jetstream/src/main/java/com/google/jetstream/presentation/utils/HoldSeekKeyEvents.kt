package com.google.jetstream.presentation.utils

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.google.jetstream.presentation.screens.videoPlayer.components.NetflixSeekDirection
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerHoldSeekState

/**
 * TV hold-to-seek keys — tap skips once; hold keeps seeking until release/OK commits or Back cancels.
 * Speed ramps automatically (1x → 2x → 5x) while held; repeated presses can bump faster.
 */
fun Modifier.handleHoldSeekKeyEvents(
    holdSeekState: VideoPlayerHoldSeekState,
    enabled: () -> Boolean = { true },
    onStartHold: (NetflixSeekDirection) -> Unit,
    onTapSeek: (NetflixSeekDirection) -> Unit,
    onCommit: () -> Unit,
    onCancel: () -> Unit,
    onBumpSpeed: (NetflixSeekDirection) -> Unit,
    onSeekKeyDown: (NetflixSeekDirection) -> Unit = {},
    onSeekKeyUp: (NetflixSeekDirection) -> Unit = {},
    onInteraction: () -> Unit = {},
): Modifier = onPreviewKeyEvent { event ->
    if (!enabled()) return@onPreviewKeyEvent false
    val native = event.nativeKeyEvent

    when (native.keyCode) {
        KeyEvent.KEYCODE_BACK -> {
            if (holdSeekState.isActive && native.action == KeyEvent.ACTION_DOWN) {
                onCancel()
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER,
        -> {
            if (holdSeekState.isActive && native.action == KeyEvent.ACTION_UP) {
                onCommit()
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT,
        -> handleHorizontalSeekKey(
            event = event,
            direction = NetflixSeekDirection.Back,
            holdSeekState = holdSeekState,
            onStartHold = onStartHold,
            onTapSeek = onTapSeek,
            onBumpSpeed = onBumpSpeed,
            onCommit = onCommit,
            onSeekKeyDown = onSeekKeyDown,
            onSeekKeyUp = onSeekKeyUp,
            onInteraction = onInteraction,
        )
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT,
        -> handleHorizontalSeekKey(
            event = event,
            direction = NetflixSeekDirection.Forward,
            holdSeekState = holdSeekState,
            onStartHold = onStartHold,
            onTapSeek = onTapSeek,
            onBumpSpeed = onBumpSpeed,
            onCommit = onCommit,
            onSeekKeyDown = onSeekKeyDown,
            onSeekKeyUp = onSeekKeyUp,
            onInteraction = onInteraction,
        )
        else -> false
    }
}

private fun handleHorizontalSeekKey(
    event: androidx.compose.ui.input.key.KeyEvent,
    direction: NetflixSeekDirection,
    holdSeekState: VideoPlayerHoldSeekState,
    onStartHold: (NetflixSeekDirection) -> Unit,
    onTapSeek: (NetflixSeekDirection) -> Unit,
    onBumpSpeed: (NetflixSeekDirection) -> Unit,
    onCommit: () -> Unit,
    onSeekKeyDown: (NetflixSeekDirection) -> Unit,
    onSeekKeyUp: (NetflixSeekDirection) -> Unit,
    onInteraction: () -> Unit,
): Boolean {
    val native = event.nativeKeyEvent
    when (native.action) {
        KeyEvent.ACTION_DOWN -> {
            onInteraction()
            if (holdSeekState.isActive) {
                onBumpSpeed(direction)
                return true
            }
            onSeekKeyDown(direction)
            if (native.repeatCount >= 1) {
                holdSeekState.clearPendingTap()
                onStartHold(direction)
                return true
            }
            holdSeekState.markPendingTap(direction)
            return true
        }
        KeyEvent.ACTION_UP -> {
            onSeekKeyUp(direction)
            if (holdSeekState.isActive) {
                onCommit()
                return true
            }
            if (holdSeekState.pendingTapDirection == direction) {
                onTapSeek(direction)
            }
            holdSeekState.clearPendingTap()
            return true
        }
        else -> return false
    }
}
