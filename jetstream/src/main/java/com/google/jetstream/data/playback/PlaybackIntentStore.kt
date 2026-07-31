package com.google.jetstream.data.playback

import javax.inject.Inject
import javax.inject.Singleton

/** Holds the next playback session set by movie detail before navigating to the player. */
@Singleton
class PlaybackIntentStore @Inject constructor() {

    private var pending: PlaybackIntent? = null

    fun set(intent: PlaybackIntent) {
        pending = intent
    }

    fun consume(): PlaybackIntent? = pending.also { pending = null }

    fun peek(): PlaybackIntent? = pending
}

data class PlaybackIntent(
    val movieSlug: String,
    val title: String,
    val hlsUrl: String,
    val initialTimeMs: Long = 0L,
    val isTrailer: Boolean = false,
    val vodAssetId: Int? = null,
    val bunnyVideoId: String? = null,
    val isDrm: Boolean = false,
)
