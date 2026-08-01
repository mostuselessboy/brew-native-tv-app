package com.google.jetstream.data.util

import kotlin.math.ceil
import kotlin.math.floor

/** Bunny seek sprite sheets — mirrors mobile-viewer `seekSpritePrefetch.ts`. */
object SeekSpritePreview {

    const val SPRITE_COLUMNS = 6
    const val SPRITE_ROWS = 6
    const val SPRITE_FRAMES = SPRITE_COLUMNS * SPRITE_ROWS
    const val DEFAULT_SPRITE_WIDTH = 960
    const val DEFAULT_SPRITE_HEIGHT = 540
    const val MAX_PREVIEW_WIDTH_PX = 200

    fun normalizeVideoId(raw: String?): String =
        raw.orEmpty().trim().removePrefix("/")

    fun spriteUrl(cdnZone: String, videoId: String, spriteIndex: Int): String {
        val zone = cdnZone.trim().ifBlank { "vz-d84551ab-b23" }
        val id = normalizeVideoId(videoId)
        return "https://$zone.b-cdn.net/$id/seek/_$spriteIndex.jpg?width=480"
    }

    fun intervalForDuration(durationSeconds: Double): Double =
        if (durationSeconds <= 60) {
            durationSeconds / SPRITE_FRAMES
        } else {
            2.0
        }

    fun spriteIndexForTime(timeSeconds: Double, durationSeconds: Double): Int {
        if (durationSeconds <= 0) return 0
        val interval = intervalForDuration(durationSeconds)
        val frameIndex = floor(timeSeconds.coerceAtLeast(0.0) / interval).toInt()
        return frameIndex / SPRITE_FRAMES
    }

    fun frameInSprite(timeSeconds: Double, durationSeconds: Double): Int {
        if (durationSeconds <= 0) return 0
        val interval = intervalForDuration(durationSeconds)
        val frameIndex = floor(timeSeconds.coerceAtLeast(0.0) / interval).toInt()
        return frameIndex % SPRITE_FRAMES
    }

    fun totalSpriteCount(durationSeconds: Double): Int {
        if (durationSeconds <= 0) return 0
        val interval = intervalForDuration(durationSeconds)
        val totalFrames = ceil(durationSeconds / interval).toInt()
        return ceil(totalFrames.toDouble() / SPRITE_FRAMES).toInt()
    }

    data class FramePreview(
        val spriteUrl: String,
        val spriteIndex: Int,
        val offsetX: Float,
        val offsetY: Float,
        val frameWidth: Float,
        val frameHeight: Float,
        val previewWidth: Float,
        val previewHeight: Float,
        val scale: Float,
    )

    fun framePreview(
        timeSeconds: Double,
        durationSeconds: Double,
        videoId: String,
        cdnZone: String,
        spriteWidth: Int = DEFAULT_SPRITE_WIDTH,
        spriteHeight: Int = DEFAULT_SPRITE_HEIGHT,
    ): FramePreview? {
        val id = normalizeVideoId(videoId)
        if (id.isBlank() || durationSeconds <= 0) return null

        val spriteIndex = spriteIndexForTime(timeSeconds, durationSeconds)
        val frameInSprite = frameInSprite(timeSeconds, durationSeconds)
        val col = frameInSprite % SPRITE_COLUMNS
        val row = frameInSprite / SPRITE_COLUMNS

        val frameW = spriteWidth.toFloat() / SPRITE_COLUMNS
        val frameH = spriteHeight.toFloat() / SPRITE_ROWS
        val aspect = frameW / frameH

        var previewW = MAX_PREVIEW_WIDTH_PX.toFloat()
        var previewH = previewW / aspect
        if (previewH > 160f) {
            previewH = 160f
            previewW = previewH * aspect
        }

        val scale = minOf(previewW / frameW, previewH / frameH)
        val offsetX = -(col * frameW) * scale
        val offsetY = -(row * frameH) * scale

        return FramePreview(
            spriteUrl = spriteUrl(cdnZone, id, spriteIndex),
            spriteIndex = spriteIndex,
            offsetX = offsetX,
            offsetY = offsetY,
            frameWidth = frameW,
            frameHeight = frameH,
            previewWidth = frameW * scale,
            previewHeight = frameH * scale,
            scale = scale,
        )
    }
}
