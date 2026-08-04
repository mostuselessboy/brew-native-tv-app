package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi

data class SubtitleTrackOption(
    val id: String,
    val label: String,
    val group: TrackGroup,
    val trackIndex: Int,
)

data class QualityTrackOption(
    val id: String,
    val label: String,
    val height: Int,
)

data class PlaybackSpeedOption(
    val speed: Float,
    val label: String,
)

@OptIn(UnstableApi::class)
object VideoPlayerTrackHelper {

    val PLAYBACK_SPEEDS = listOf(
        PlaybackSpeedOption(0.5f, "0.5x"),
        PlaybackSpeedOption(0.75f, "0.75x"),
        PlaybackSpeedOption(1f, "1x"),
        PlaybackSpeedOption(1.25f, "1.25x"),
        PlaybackSpeedOption(1.5f, "1.5x"),
        PlaybackSpeedOption(2f, "2x"),
    )

    fun readSubtitleTracks(player: Player): List<SubtitleTrackOption> {
        val options = mutableListOf<SubtitleTrackOption>()
        player.currentTracks.groups.forEach { groupInfo ->
            if (groupInfo.type != C.TRACK_TYPE_TEXT) return@forEach
            val group = groupInfo.mediaTrackGroup
            for (index in 0 until group.length) {
                val format = group.getFormat(index)
                val label = formatLabel(format)
                options.add(
                    SubtitleTrackOption(
                        id = "${group.id}_$index",
                        label = label,
                        group = group,
                        trackIndex = index,
                    ),
                )
            }
        }
        return options.distinctBy { it.label }
    }

    fun readQualityTracks(player: Player): List<QualityTrackOption> {
        val heights = linkedMapOf<Int, QualityTrackOption>()
        player.currentTracks.groups.forEach { groupInfo ->
            if (groupInfo.type != C.TRACK_TYPE_VIDEO) return@forEach
            val group = groupInfo.mediaTrackGroup
            for (index in 0 until group.length) {
                val height = group.getFormat(index).height
                if (height <= 0 || height in heights) continue
                heights[height] = QualityTrackOption(
                    id = "q_$height",
                    label = qualityDisplayLabel(height),
                    height = height,
                )
            }
        }
        return heights.values.sortedByDescending { it.height }
    }

    fun autoQualityLabel(player: Player, options: List<QualityTrackOption>): String = "Auto"

    private fun currentVideoHeight(player: Player, options: List<QualityTrackOption>): Int? =
        selectedVideoHeight(player) ?: selectedQualityHeight(player, options)

    fun qualityDisplayLabel(height: Int): String = resolutionLabel(height)

    private fun resolutionLabel(height: Int): String = "${height}p"

    fun speedPillLabel(player: Player): String {
        val speed = player.playbackParameters.speed
        return if (speed % 1f == 0f) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    fun subtitlePillLabel(player: Player): String {
        if (isSubtitlesOff(player)) return "Off"
        val options = readSubtitleTracks(player)
        val selectedId = selectedSubtitleId(player, options) ?: return "On"
        return options.firstOrNull { it.id == selectedId }?.label?.take(12) ?: "On"
    }

    fun qualityPillLabel(player: Player): String {
        val options = readQualityTracks(player)
        val maxHeight = player.trackSelectionParameters.maxVideoHeight
        val isAuto = maxHeight == Int.MAX_VALUE || maxHeight <= 0

        if (!isAuto) {
            val cappedHeight = options.firstOrNull { it.height == maxHeight }?.height
                ?: options.minByOrNull { kotlin.math.abs(it.height - maxHeight) }?.height
                ?: maxHeight
            return resolutionLabel(cappedHeight)
        }

        return "Auto"
    }

    private fun selectedVideoHeight(player: Player): Int? {
        player.currentTracks.groups.forEach { groupInfo ->
            if (groupInfo.type != C.TRACK_TYPE_VIDEO) return@forEach
            for (index in 0 until groupInfo.length) {
                if (!groupInfo.isTrackSelected(index)) continue
                val height = groupInfo.mediaTrackGroup.getFormat(index).height
                if (height > 0) return height
            }
        }
        val renderedHeight = player.videoSize.height
        return renderedHeight.takeIf { it > 0 }
    }

    fun isSubtitlesOff(player: Player): Boolean {
        return player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
    }

    fun selectedSubtitleId(player: Player, options: List<SubtitleTrackOption>): String? {
        if (isSubtitlesOff(player)) return null
        val overrides = player.trackSelectionParameters.overrides
        options.forEach { option ->
            val override = overrides[option.group]
            if (override != null && option.trackIndex in override.trackIndices) {
                return option.id
            }
        }
        player.currentTracks.groups.forEach { groupInfo ->
            if (groupInfo.type != C.TRACK_TYPE_TEXT) return@forEach
            for (index in 0 until groupInfo.length) {
                if (!groupInfo.isTrackSelected(index)) continue
                val id = "${groupInfo.mediaTrackGroup.id}_$index"
                if (options.any { it.id == id }) return id
            }
        }
        return null
    }

    fun selectedQualityHeight(player: Player, options: List<QualityTrackOption>): Int? {
        val maxHeight = player.trackSelectionParameters.maxVideoHeight
        if (maxHeight == Int.MAX_VALUE) return null
        return options.firstOrNull { it.height == maxHeight }?.height
            ?: options.minByOrNull { kotlin.math.abs(it.height - maxHeight) }?.height
    }

    fun selectSubtitlesOff(player: Player) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .build()
    }

    fun selectSubtitle(player: Player, option: SubtitleTrackOption) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .setOverrideForType(TrackSelectionOverride(option.group, option.trackIndex))
            .build()
    }

    fun selectQualityAuto(player: Player) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
            .build()
    }

    fun selectQuality(player: Player, height: Int) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .setMaxVideoSize(Int.MAX_VALUE, height)
            .build()
    }

    fun setPlaybackSpeed(player: Player, speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    private fun formatLabel(format: Format): String {
        format.label?.takeIf { it.isNotBlank() }?.let { return it }
        format.language?.takeIf { it.isNotBlank() }?.let { return it }
        return "Subtitle"
    }
}
