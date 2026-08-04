package com.google.jetstream.data.playback

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Notifies catalog/detail screens to refresh after playback progress is saved. */
@Singleton
class PlaybackProgressNotifier @Inject constructor() {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    @Volatile
    private var pendingRefresh = false

    fun onCheckpointSaved() {
        pendingRefresh = true
        _events.tryEmit(Unit)
    }

    fun consumePendingRefresh(): Boolean {
        val pending = pendingRefresh
        pendingRefresh = false
        return pending
    }
}
