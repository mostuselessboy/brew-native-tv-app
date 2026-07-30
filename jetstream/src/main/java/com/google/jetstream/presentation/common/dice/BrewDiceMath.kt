package com.google.jetstream.presentation.common.dice

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

internal object BrewDiceConstants {
    const val FRONT_CARD_INDEX = 0
    const val FRONT_CARD_DIST = 0.35f
    const val SETTLE_LOOPS = 2
    const val ARC_ANGLE = 0.24f
    const val FAN_X = 3.05f
    const val RY_PER_CARD = 13f
    const val RY_CLAMP = 38f
    const val RZ_TILT = 0.62f
    const val VISIBLE_FADE = 3.1f
    const val DEPTH_Z_MULT = 72f
}

internal data class CardArcPose(
    val tx: Float,
    val ty: Float,
    val rz: Float,
    val ry: Float,
    val dist: Float,
)

internal fun wrapRelativeIndex(index: Int, spin: Float, count: Int): Float {
    var rel = index - spin
    rel = ((rel % count) + count) % count
    if (rel > count / 2f) rel -= count
    return rel
}

internal fun cardArcPose(
    rel: Float,
    introVal: Float,
    cardW: Float,
    cardH: Float,
): CardArcPose {
    val dist = abs(rel)
    val angle = rel * BrewDiceConstants.ARC_ANGLE * introVal
    val tx = sin(angle) * (cardW * BrewDiceConstants.FAN_X)
    val ty = (1 - cos(angle)) * (cardH * 0.78f)
    val rz = angle
    var ry = -rel * BrewDiceConstants.RY_PER_CARD * introVal
    ry = ry.coerceIn(-BrewDiceConstants.RY_CLAMP, BrewDiceConstants.RY_CLAMP)
    return CardArcPose(tx, ty, rz, ry, dist)
}

internal fun cardOpacityForDist(dist: Float, introVal: Float): Float {
    val opacity = when {
        dist <= 1.1f -> lerp(dist, 0f, 1.1f, 1f, 0.62f)
        dist <= 2.2f -> lerp(dist, 1.1f, 2.2f, 0.62f, 0.18f)
        dist <= BrewDiceConstants.VISIBLE_FADE -> lerp(
            dist,
            2.2f,
            BrewDiceConstants.VISIBLE_FADE,
            0.18f,
            0f,
        )
        else -> 0f
    }
    return opacity * introVal
}

internal fun frontScore(dist: Float, plateau: Float = 0.48f): Float =
    exp(-(dist * dist) / (plateau * plateau))

internal fun cardScaleForDist(dist: Float): Float {
    val score = frontScore(dist)
    return lerp(score, 0f, 0.7f, 1f, 0.66f, 0.8f, 0.9f)
}

internal fun stackZIndex(dist: Float, count: Int, mult: Float): Float {
    if (dist < BrewDiceConstants.FRONT_CARD_DIST) return 10_000f
    val frontScore = exp(-(dist * dist) / (0.48f * 0.48f))
    return frontScore * count * mult
}

internal fun cardIsAtFront(rel: Float): Boolean =
    abs(rel) < BrewDiceConstants.FRONT_CARD_DIST

internal fun sortIndicesByFrontDistance(indices: List<Int>, frontIndex: Int, count: Int): List<Int> =
    indices.sortedByDescending { index ->
        var dist = abs(index - frontIndex).toFloat()
        if (dist > count / 2f) dist = count - dist
        dist
    }

internal fun computeSpinTarget(current: Float, winnerIndex: Int, count: Int): Float {
    var target = current.roundToInt() + BrewDiceConstants.SETTLE_LOOPS * count
    val delta = (((winnerIndex - (target % count)) % count) + count) % count
    return (target + delta).toFloat()
}

private fun lerp(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
    if (inMax == inMin) return outMin
    val t = ((value - inMin) / (inMax - inMin)).coerceIn(0f, 1f)
    return outMin + t * (outMax - outMin)
}

private fun lerp(score: Float, s0: Float, s1: Float, s2: Float, v0: Float, v1: Float, v2: Float): Float {
    return when {
        score <= s0 -> v0
        score <= s1 -> lerp(score, s0, s1, v0, v1)
        score <= s2 -> lerp(score, s1, s2, v1, v2)
        else -> v2
    }
}
