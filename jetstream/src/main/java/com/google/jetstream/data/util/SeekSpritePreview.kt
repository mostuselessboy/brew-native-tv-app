package com.google.jetstream.data.util

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

/** Bunny seek sprite sheets — mirrors tv-app `calculateSpriteInfo` / mobile `seekSpritePrefetch`. */
object SeekSpritePreview {

    const val SPRITE_COLUMNS = 6
    const val SPRITE_ROWS = 6
    const val SPRITE_FRAMES = SPRITE_COLUMNS * SPRITE_ROWS

    /** Sprite sheet pixel size when loaded via `?width=480` CDN param. */
    const val CDN_SPRITE_WIDTH = 480
    const val CDN_SPRITE_HEIGHT = 270

    const val DEFAULT_PREVIEW_WIDTH_PX = 280
    const val DEFAULT_PREVIEW_HEIGHT_PX = 200

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
        val sheetWidth: Float,
        val sheetHeight: Float,
        val previewWidth: Float,
        val previewHeight: Float,
        val scale: Float,
    )

    fun framePreview(
        timeSeconds: Double,
        durationSeconds: Double,
        videoId: String,
        cdnZone: String,
        spriteWidth: Int = CDN_SPRITE_WIDTH,
        spriteHeight: Int = CDN_SPRITE_HEIGHT,
        maxPreviewWidthPx: Int = DEFAULT_PREVIEW_WIDTH_PX,
        maxPreviewHeightPx: Int = DEFAULT_PREVIEW_HEIGHT_PX,
    ): FramePreview? {
        val id = normalizeVideoId(videoId)
        if (id.isBlank() || durationSeconds <= 0) return null

        val spriteIndex = spriteIndexForTime(timeSeconds, durationSeconds)
        val frameInSprite = frameInSprite(timeSeconds, durationSeconds)
        val col = frameInSprite % SPRITE_COLUMNS
        val row = frameInSprite / SPRITE_COLUMNS

        val frameW = spriteWidth.toFloat() / SPRITE_COLUMNS
        val frameH = spriteHeight.toFloat() / SPRITE_ROWS
        val frameAspectRatio = frameW / frameH

        // Match mobile-viewer `buildSpritePreviewInfo`: fit one frame inside max bounds (contain).
        var fitW = maxPreviewWidthPx.toFloat()
        var fitH = fitW / frameAspectRatio
        if (fitH > maxPreviewHeightPx) {
            fitH = maxPreviewHeightPx.toFloat()
            fitW = fitH * frameAspectRatio
        }

        val scale = min(fitW / frameW, fitH / frameH)

        val offsetX = round(-(col * frameW) * scale)
        val offsetY = round(-(row * frameH) * scale)

        return FramePreview(
            spriteUrl = spriteUrl(cdnZone, id, spriteIndex),
            spriteIndex = spriteIndex,
            offsetX = offsetX,
            offsetY = offsetY,
            sheetWidth = spriteWidth.toFloat(),
            sheetHeight = spriteHeight.toFloat(),
            previewWidth = round(frameW * scale),
            previewHeight = round(frameH * scale),
            scale = scale,
        )
    }

    fun stripFrameTimes(
        centerTimeSeconds: Double,
        durationSeconds: Double,
        frameCount: Int = 7,
        stepSeconds: Double = 10.0,
    ): List<Double> {
        if (durationSeconds <= 0 || frameCount <= 0) return emptyList()
        val centerIndex = frameCount / 2
        return List(frameCount) { index ->
            val offset = (index - centerIndex) * stepSeconds
            (centerTimeSeconds + offset).coerceIn(0.0, durationSeconds)
        }
    }

    fun spriteUrlsToWarm(
        durationSeconds: Double,
        videoId: String,
        cdnZone: String,
        maxSheets: Int = 32,
    ): List<String> {
        val id = normalizeVideoId(videoId)
        if (id.isBlank() || durationSeconds <= 0) return emptyList()
        val total = totalSpriteCount(durationSeconds).coerceAtLeast(1)
        return (0 until minOf(total, maxSheets)).map { index ->
            spriteUrl(cdnZone, id, index)
        }
    }
}
