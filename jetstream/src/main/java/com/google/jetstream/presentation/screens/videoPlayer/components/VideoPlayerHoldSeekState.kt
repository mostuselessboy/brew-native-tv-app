package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.Player

/** Hold-to-seek session — keeps scrubbing until OK commits or Back cancels. */
class VideoPlayerHoldSeekState {
    var isActive by mutableStateOf(false)
        private set

    var direction by mutableStateOf<NetflixSeekDirection?>(null)
        private set

    var positionMs by mutableLongStateOf(0L)
        private set

    var anchorMs by mutableLongStateOf(0L)
        private set

    var speed by mutableIntStateOf(1)
        private set

    var wasPlaying by mutableStateOf(false)
        private set

    var pendingTapDirection by mutableStateOf<NetflixSeekDirection?>(null)
        private set

    private var holdStartedAtMs = 0L
    private var manualSpeedFloor = 1

    fun clearPendingTap() {
        pendingTapDirection = null
    }

    fun markPendingTap(direction: NetflixSeekDirection) {
        pendingTapDirection = direction
    }

    fun begin(direction: NetflixSeekDirection, player: Player) {
        if (isActive && this.direction == direction) return
        val current = player.currentPosition
        isActive = true
        this.direction = direction
        anchorMs = current
        positionMs = current
        speed = 1
        manualSpeedFloor = 1
        holdStartedAtMs = System.currentTimeMillis()
        wasPlaying = player.playWhenReady
        player.pause()
        player.playWhenReady = false
    }

    /** Ramp 1x → 2x → 5x automatically while the seek key stays held. */
    fun syncAutoSpeed() {
        if (!isActive) return
        val elapsedMs = (System.currentTimeMillis() - holdStartedAtMs).coerceAtLeast(0L)
        val autoSpeed = when {
            elapsedMs >= 2_800 -> 5
            elapsedMs >= 1_400 -> 2
            else -> 1
        }
        speed = maxOf(autoSpeed, manualSpeedFloor)
    }

    fun bumpSpeed(inputDirection: NetflixSeekDirection) {
        if (!isActive || direction == null) return
        val seekingForward = direction == NetflixSeekDirection.Forward
        val increasing = when {
            seekingForward && inputDirection == NetflixSeekDirection.Forward -> true
            !seekingForward && inputDirection == NetflixSeekDirection.Back -> true
            else -> false
        }
        val bumped = when {
            increasing -> when (speed) {
                1 -> 2
                2 -> 5
                else -> 5
            }
            else -> when (speed) {
                5 -> 2
                2 -> 1
                else -> 1
            }
        }
        manualSpeedFloor = bumped
        speed = bumped
    }

    /** @return false when a boundary was hit and seeking should stop. */
    fun tick(durationMs: Long): Boolean {
        if (!isActive || direction == null || durationMs <= 0L) return false
        val sign = if (direction == NetflixSeekDirection.Forward) 1 else -1
        val deltaMs = 1000L * speed * sign
        val next = (positionMs + deltaMs).coerceIn(0L, durationMs)
        positionMs = next
        if (next == 0L || next == durationMs) {
            return false
        }
        return true
    }

    fun syncPlayer(player: Player) {
        if (!isActive) return
        player.seekTo(positionMs)
    }

    fun commit(player: Player) {
        if (!isActive) return
        val resume = wasPlaying
        val targetMs = positionMs
        reset()
        player.seekTo(targetMs)
        if (resume) {
            player.playWhenReady = true
            player.play()
        }
    }

    fun cancel(player: Player) {
        if (!isActive) return
        val resume = wasPlaying
        val targetMs = anchorMs
        reset()
        player.seekTo(targetMs)
        if (resume) {
            player.playWhenReady = true
            player.play()
        }
    }

    fun reset() {
        isActive = false
        direction = null
        speed = 1
        manualSpeedFloor = 1
        holdStartedAtMs = 0L
    }
}
