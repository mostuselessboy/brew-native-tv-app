package com.google.jetstream.presentation.common.dice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.common.BrewCinematicBackground
import com.google.jetstream.presentation.theme.BrewDisplay
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle

private val SpinEasing = CubicBezierEasing(0.3f, 0f, 0.12f, 1f)
private val HeroFocusEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val FlipEasing = CubicBezierEasing(0.42f, 0f, 0.18f, 1f)

private const val SPIN_MS = 2600
private const val SOLO_HOLD_MS = 320
private const val FAN_OUT_MS = 420
private const val POST_SPIN_HOLD_MS = 520
private const val HERO_FOCUS_MS = 720
private const val FLIP_MS = 920
private const val SOLO_ENTER_MS = 520
private const val TAGLINE_ROTATE_MS = 2600L

private val DiceTaglineGold = Color(0xFFF5DC8A)
private val DiceAccentGold = Color(0xFFFFC15E)
private val CardBackOrange = Color(0xFFDA492B)
private val CardBackDark = Color(0xFF181410)
private val BrewCardAspect = 1024f / 724f

private val DiceTeasers = listOf(
    "Shuffling the shelf…",
    "Meet an unsung masterwork…",
    "Pulling something rare…",
    "Reading the room…",
    "Almost there…",
)

private enum class DicePhase {
    Idle,
    Solo,
    Fanning,
    Spinning,
    Settling,
    Focusing,
    Flipping,
    Revealed,
}

@Composable
fun BrewDicePickerDialog(
    visible: Boolean,
    movies: MovieList,
    onDismiss: () -> Unit,
    onWatchNow: (Movie) -> Unit,
) {
    if (!visible || movies.isEmpty()) return

    var runId by remember { mutableIntStateOf(0) }
    var roll by remember { mutableStateOf<BrewDiceRoll?>(null) }

    LaunchedEffect(visible, runId, movies) {
        if (visible) {
            roll = buildDiceRoll(movies)
        }
    }

    val currentRoll = roll ?: return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        BackHandler(onBack = onDismiss)

        BrewCinematicBackground {
            BrewDicePickerStage(
            runId = runId,
            roll = currentRoll,
            onWatchNow = { onWatchNow(currentRoll.winner.movie) },
            onPickAnother = { runId++ },
            )
        }
    }
}

@Composable
private fun BrewDicePickerStage(
    runId: Int,
    roll: BrewDiceRoll,
    onWatchNow: () -> Unit,
    onPickAnother: () -> Unit,
) {
    val deck = roll.deck
    val winnerIndex = roll.winnerIndex
    val count = deck.size

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenW = config.screenWidthDp.dp
    val cardW = minOf(132.dp, screenW * 0.115f)
    val cardH = cardW * BrewCardAspect
    val cardWPx = with(density) { cardW.toPx() }
    val cardHPx = with(density) { cardH.toPx() }

    var phase by remember { mutableStateOf(DicePhase.Idle) }
    var frontIndex by remember { mutableIntStateOf(BrewDiceConstants.FRONT_CARD_INDEX) }
    var teaserIndex by remember { mutableIntStateOf(0) }
    var showWinnerTagline by remember { mutableStateOf(false) }

    val spin = remember { Animatable(BrewDiceConstants.FRONT_CARD_INDEX.toFloat()) }
    val fanOut = remember { Animatable(0f) }
    val intro = remember { Animatable(1f) }
    val heroFocus = remember { Animatable(0f) }
    val flipProgress = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }
    val soloEnterY = remember { Animatable(1f) }
    val spinSettled = remember { mutableFloatStateOf(0f) }
    val spinStarted = remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "diceFloat")
    val soloFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "soloFloat",
    )
    val heroFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "heroFloat",
    )

    val watchNowFocus = remember { FocusRequester() }

    LaunchedEffect(runId, count) {
        phase = DicePhase.Idle
        showWinnerTagline = false
        teaserIndex = 0
        spinSettled.floatValue = 0f
        spinStarted.floatValue = 0f
        frontIndex = BrewDiceConstants.FRONT_CARD_INDEX

        spin.snapTo(BrewDiceConstants.FRONT_CARD_INDEX.toFloat())
        fanOut.snapTo(0f)
        intro.snapTo(1f)
        heroFocus.snapTo(0f)
        flipProgress.snapTo(0f)
        reveal.snapTo(0f)
        soloEnterY.snapTo(1f)

        phase = DicePhase.Solo
        soloEnterY.animateTo(0f, tween(SOLO_ENTER_MS, easing = FastOutSlowInEasing))

        delay(SOLO_HOLD_MS.toLong())
        phase = DicePhase.Fanning
        fanOut.animateTo(1f, tween(FAN_OUT_MS, easing = FastOutSlowInEasing))

        phase = DicePhase.Spinning
        spinStarted.floatValue = 1f
        val target = computeSpinTarget(spin.value, winnerIndex, count)
        spin.animateTo(target, tween(SPIN_MS, easing = SpinEasing))
        spinSettled.floatValue = 1f

        phase = DicePhase.Settling
        delay(POST_SPIN_HOLD_MS.toLong())

        phase = DicePhase.Focusing
        heroFocus.animateTo(1f, tween(HERO_FOCUS_MS, easing = HeroFocusEasing))

        phase = DicePhase.Flipping
        reveal.animateTo(1f, tween(FLIP_MS, easing = FlipEasing))
        flipProgress.animateTo(1f, tween(FLIP_MS, easing = FlipEasing))

        phase = DicePhase.Revealed
        showWinnerTagline = true
        watchNowFocus.requestFocus()
    }

    LaunchedEffect(phase) {
        if (phase == DicePhase.Spinning || phase == DicePhase.Fanning || phase == DicePhase.Solo) {
            while (phase != DicePhase.Revealed && phase != DicePhase.Flipping &&
                phase != DicePhase.Focusing && phase != DicePhase.Settling
            ) {
                delay(TAGLINE_ROTATE_MS)
                if (phase == DicePhase.Revealed) break
                teaserIndex = (teaserIndex + 1) % DiceTeasers.size
            }
        }
    }

    LaunchedEffect(spin.value, count) {
        var best = 0
        var bestDist = count.toFloat()
        for (i in 0 until count) {
            val rel = wrapRelativeIndex(i, spin.value, count)
            val d = kotlin.math.abs(rel)
            if (d < bestDist) {
                bestDist = d
                best = i
            }
        }
        frontIndex = best
    }

    val sortOrder = remember(frontIndex, count, fanOut.value) {
        if (fanOut.value < 0.99f) {
            listOf(BrewDiceConstants.FRONT_CARD_INDEX)
        } else {
            sortIndicesByFrontDistance(deck.indices.toList(), frontIndex, count)
        }
    }

    val showActions = phase == DicePhase.Revealed || flipProgress.value > 0.5f
    val actionsAlpha = ((flipProgress.value - 0.52f) / 0.2f).coerceIn(0f, 1f)

    BrewCinematicBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(0.12f))

            if (!showWinnerTagline) {
                Text(
                    text = DiceTeasers[teaserIndex],
                    color = DiceTaglineGold,
                    fontFamily = BrewDisplay,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .fillMaxWidth(),
                )
            } else {
                Text(
                    text = roll.winner.tagline,
                    color = DiceTaglineGold,
                    fontFamily = BrewDisplay,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = reveal.value
                        },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val floatAmp = cardHPx * 0.034f
                val heroFloatAmp = cardHPx * 0.036f

                sortOrder.forEach { index ->
                    val movie = deck[index]
                    val isWinner = index == winnerIndex
                    val rel = wrapRelativeIndex(index, spin.value, count)
                    val atFront = cardIsAtFront(rel)
                    val pose = cardArcPose(rel, intro.value, cardWPx, cardHPx)

                    val cardStyle = computeCardTransform(
                        index = index,
                        isWinner = isWinner,
                        rel = rel,
                        atFront = atFront,
                        pose = pose,
                        fanOut = fanOut.value,
                        spinStarted = spinStarted.floatValue,
                        spinSettled = spinSettled.floatValue,
                        heroFocus = heroFocus.value,
                        flipProgress = flipProgress.value,
                        reveal = reveal.value,
                        soloEnterY = soloEnterY.value,
                        soloFloat = soloFloat,
                        heroFloat = heroFloat,
                        cardWPx = cardWPx,
                        cardHPx = cardHPx,
                        count = count,
                        introVal = intro.value,
                    )

                    DiceCard(
                        movie = movie,
                        isWinner = isWinner,
                        cardW = cardW,
                        cardH = cardH,
                        flipProgress = if (isWinner) flipProgress.value else 0f,
                        heroLevitate = isWinner && heroFocus.value > 0.5f,
                        heroFloatOffset = if (isWinner && heroFocus.value > 0.5f) {
                            lerp(heroFloat, 0f, 1f, -heroFloatAmp, heroFloatAmp)
                        } else {
                            0f
                        },
                        modifier = Modifier
                            .zIndex(cardStyle.zIndex)
                            .graphicsLayer {
                                alpha = cardStyle.alpha
                                translationX = cardStyle.tx
                                translationY = cardStyle.ty + cardStyle.soloFloatY
                                rotationY = cardStyle.ry
                                rotationZ = cardStyle.rz * BrewDiceConstants.RZ_TILT * (180f / Math.PI.toFloat())
                                scaleX = cardStyle.scale
                                scaleY = cardStyle.scale
                                cameraDistance = 12f * density.density
                            },
                    )
                }
            }

            if (showActions) {
                DiceResultActions(
                    alpha = actionsAlpha,
                    watchNowFocus = watchNowFocus,
                    onWatchNow = onWatchNow,
                    onPickAnother = onPickAnother,
                    modifier = Modifier.padding(bottom = 48.dp),
                )
            } else {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

private data class CardTransform(
    val tx: Float,
    val ty: Float,
    val rz: Float,
    val ry: Float,
    val scale: Float,
    val alpha: Float,
    val zIndex: Float,
    val soloFloatY: Float,
)

private fun computeCardTransform(
    index: Int,
    isWinner: Boolean,
    rel: Float,
    atFront: Boolean,
    pose: CardArcPose,
    fanOut: Float,
    spinStarted: Float,
    spinSettled: Float,
    heroFocus: Float,
    flipProgress: Float,
    reveal: Float,
    soloEnterY: Float,
    soloFloat: Float,
    heroFloat: Float,
    cardWPx: Float,
    cardHPx: Float,
    count: Int,
    introVal: Float,
): CardTransform {
    val floatAmp = cardHPx * 0.034f

    if (heroFocus > 0f || flipProgress > 0f) {
        if (isWinner) {
            val baseScale = cardScaleForDist(pose.dist)
            val scale = lerp(heroFocus, 0f, 1f, baseScale, 1.18f)
            val tx = lerp(heroFocus, 0f, 1f, pose.tx, 0f)
            val ty = lerp(heroFocus, 0f, 1f, pose.ty, pose.ty - cardHPx * 0.05f)
            return CardTransform(
                tx = tx,
                ty = ty,
                rz = lerp(heroFocus, 0f, 1f, pose.rz, 0f),
                ry = lerp(heroFocus, 0f, 1f, pose.ry, 0f),
                scale = scale,
                alpha = 1f,
                zIndex = 10_000f,
                soloFloatY = 0f,
            )
        }
        val fade = lerp(heroFocus, 0f, 0.55f, 1f, 0f)
        return CardTransform(
            tx = pose.tx,
            ty = pose.ty,
            rz = pose.rz,
            ry = pose.ry,
            scale = cardScaleForDist(pose.dist) * introVal * lerp(heroFocus, 0f, 1f, 1f, 0.9f),
            alpha = cardOpacityForDist(pose.dist, introVal) * fade,
            zIndex = stackZIndex(pose.dist, count, BrewDiceConstants.DEPTH_Z_MULT),
            soloFloatY = 0f,
        )
    }

    if (spinSettled > 0f && isWinner) {
        val frontPose = cardArcPose(0f, introVal, cardWPx, cardHPx)
        return CardTransform(
            tx = frontPose.tx,
            ty = frontPose.ty,
            rz = frontPose.rz,
            ry = frontPose.ry,
            scale = 0.93f * lerp(introVal, 0f, 1f, 0.55f, 1f),
            alpha = 1f,
            zIndex = 10_000f,
            soloFloatY = 0f,
        )
    }

    if (spinStarted < 0.5f) {
        if (isWinner && fanOut < 0.01f) {
            return CardTransform(0f, 0f, 0f, 0f, 0.9f, 0f, 0f, 0f)
        }

        val fullScale = cardScaleForDist(pose.dist)
        val soloScale = if (atFront) 0.96f else 0.9f
        val fanForMotion = if (atFront || index == BrewDiceConstants.FRONT_CARD_INDEX) fanOut else fanOut
        val opacity = if (atFront) {
            1f
        } else {
            cardOpacityForDist(pose.dist, introVal) *
                lerp(fanOut, 0.18f, 0.55f, 1f, 0f, 0.4f, 1f)
        }

        val soloFloatY = if (atFront && fanOut < 0.85f) {
            lerp(soloFloat, 0f, 1f, -floatAmp, floatAmp) * (1 - fanOut)
        } else {
            0f
        }
        val enterOffset = if (atFront) soloEnterY * cardHPx * 0.78f * (1 - fanOut) else 0f

        return CardTransform(
            tx = lerp(fanForMotion, 0f, 1f, 0f, pose.tx),
            ty = lerp(fanForMotion, 0f, 1f, 0f, pose.ty) + enterOffset,
            rz = lerp(fanForMotion, 0f, 1f, 0f, pose.rz),
            ry = lerp(fanForMotion, 0f, 1f, 0f, pose.ry),
            scale = lerp(fanForMotion, 0f, 1f, soloScale, fullScale),
            alpha = opacity,
            zIndex = if (atFront && fanOut < 0.01f) 10_000f else stackZIndex(pose.dist, count, BrewDiceConstants.DEPTH_Z_MULT),
            soloFloatY = soloFloatY,
        )
    }

    val scale = cardScaleForDist(pose.dist) * lerp(introVal, 0f, 1f, 0.55f, 1f)
    return CardTransform(
        tx = pose.tx,
        ty = pose.ty,
        rz = pose.rz,
        ry = pose.ry,
        scale = scale,
        alpha = if (isWinner) 1f else cardOpacityForDist(pose.dist, introVal),
        zIndex = stackZIndex(pose.dist, count, BrewDiceConstants.DEPTH_Z_MULT),
        soloFloatY = 0f,
    )
}

@Composable
private fun DiceCard(
    movie: BrewDiceDeckMovie,
    isWinner: Boolean,
    cardW: androidx.compose.ui.unit.Dp,
    cardH: androidx.compose.ui.unit.Dp,
    flipProgress: Float,
    heroLevitate: Boolean,
    heroFloatOffset: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val context = LocalContext.current
    val density = LocalDensity.current
    val brewLogoSize = minOf(cardW, cardH) * 0.44f
    val posterModel = remember(movie.posterUri) {
        ImageRequest.Builder(context)
            .data(BrewImageUrl.forDiceCard(movie.posterUri))
            .size(BrewImageUrl.DICE_CARD_WIDTH, BrewImageUrl.DICE_CARD_HEIGHT)
            .crossfade(160)
            .build()
    }

    Box(
        modifier = modifier
            .size(cardW, cardH)
            .offset { IntOffset(0, heroFloatOffset.roundToInt()) },
        contentAlignment = Alignment.Center,
    ) {
        if (isWinner) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = flipProgress * 180f
                        cameraDistance = 12f * density.density
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = if (flipProgress <= 0.5f) 1f else 0f
                        }
                        .clip(shape)
                        .background(CardBackOrange),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.brew_logo),
                        contentDescription = null,
                        modifier = Modifier.size(brewLogoSize),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                            alpha = if (flipProgress > 0.5f) 1f else 0f
                        }
                        .clip(shape)
                        .background(CardBackDark),
                ) {
                    AsyncImage(
                        model = posterModel,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(CardBackDark),
            ) {
                AsyncImage(
                    model = posterModel,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun DiceResultActions(
    alpha: Float,
    watchNowFocus: FocusRequester,
    onWatchNow: () -> Unit,
    onPickAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp)
            .graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onWatchNow,
            modifier = Modifier
                .focusRequester(watchNowFocus)
                .height(48.dp)
                .widthIn(min = 200.dp),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(50)),
            contentPadding = PaddingValues(horizontal = 24.dp),
            colors = ButtonDefaults.colors(
                containerColor = Color.White,
                contentColor = Color.Black,
                focusedContainerColor = Color.White,
                focusedContentColor = Color.Black,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.watch_now),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        Button(
            onClick = onPickAnother,
            modifier = Modifier
                .height(48.dp)
                .widthIn(min = 200.dp),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(50)),
            contentPadding = PaddingValues(horizontal = 24.dp),
                colors = ButtonDefaults.colors(
                containerColor = Color(0xFF24201C),
                contentColor = DiceAccentGold,
                focusedContainerColor = Color(0xFF3A342E),
                focusedContentColor = DiceAccentGold,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.dice_brew),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.pick_another),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DiceTaglineGold,
                )
            }
        }
    }
}

private fun lerp(t: Float, t0: Float, t1: Float, v0: Float, v1: Float): Float {
    if (t1 == t0) return v0
    val clamped = ((t - t0) / (t1 - t0)).coerceIn(0f, 1f)
    return v0 + clamped * (v1 - v0)
}

private fun lerp(t: Float, t0: Float, t1: Float, t2: Float, v0: Float, v1: Float, v2: Float): Float {
    return when {
        t <= t0 -> v0
        t <= t1 -> lerp(t, t0, t1, v0, v1)
        t <= t2 -> lerp(t, t1, t2, v1, v2)
        else -> v2
    }
}
