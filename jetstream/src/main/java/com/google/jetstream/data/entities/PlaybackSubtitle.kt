package com.google.jetstream.data.entities

data class PlaybackSubtitle(
    val id: String,
    val language: String,
    val label: String,
    val url: String,
    val isDefault: Boolean = false,
)
