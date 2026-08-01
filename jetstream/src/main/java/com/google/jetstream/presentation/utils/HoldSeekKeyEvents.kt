package com.google.jetstream.presentation.utils

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.google.jetstream.presentation.screens.videoPlayer.components.NetflixSeekDirection
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerHoldSeekState

/**
 * TV hold-to-seek keys — tap skips once; hold keeps seeking until OK commits or Back cancels.
 * Same direction while seeking bumps speed (1x → 2x → 5x); opposite direction lowers it.
 */
fun Modifier.handleHoldSeekKeyEvents(
    holdSeekState: VideoPlayerHoldSeekState,
    enabled: () -> Boolean = { true },
    onStartHold: (NetflixSeekDirection) -> Unit,
    onTapSeek: (NetflixSeekDirection) -> Unit,
    onCommit: () -> Unit,
    onCancel: () -> Unit,
    onBumpSpeed: (NetflixSeekDirection) -> Unit,
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
): Boolean {
    val native = event.nativeKeyEvent
    when (native.action) {
        KeyEvent.ACTION_DOWN -> {
            if (holdSeekState.isActive) {
                onBumpSpeed(direction)
                return true
            }
            if (native.repeatCount >= 1) {
                holdSeekState.clearPendingTap()
                onStartHold(direction)
                return true
            }
            holdSeekState.markPendingTap(direction)
            return true
        }
        KeyEvent.ACTION_UP -> {
            if (holdSeekState.isActive) {
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
