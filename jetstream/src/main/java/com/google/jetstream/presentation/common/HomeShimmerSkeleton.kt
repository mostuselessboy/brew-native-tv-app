package com.google.jetstream.presentation.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.jetstream.R
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.common.ShowcaseHeight
import com.google.jetstream.presentation.screens.movies.MovieDetailTokens

private val Bone = Color(0xFF141414)
private val CardShape = RoundedCornerShape(9.dp)
private val TraySpacing = 14.dp
private val CardHeight = BrewLandscapeCardWidth * 9f / 16f

/** Static home skeleton — layout mirrors [Catalog] LazyColumn spacing. */
@Composable
fun HomeShimmerSkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(TraySpacing),
        contentPadding = PaddingValues(top = 24.dp, bottom = 56.dp),
    ) {
        item(key = "showcase") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ShowcaseHeight)
                    .padding(horizontal = padding.start)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Bone),
            )
        }

        items(count = 3, key = { "tray_$it" }) {
            SkeletonTrayRow(startPadding = padding.start)
        }
    }
}

/** Placeholder tray while continue-watching loads — same size as [MoviesRow]. */
@Composable
fun ContinueWatchingTraySkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()
    SkeletonTrayRow(
        startPadding = padding.start,
        modifier = modifier,
    )
}

@Composable
private fun SkeletonTrayRow(
    startPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(start = startPadding, bottom = 8.dp)
                .width(140.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Bone),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight)
                .padding(start = startPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(BrewLandscapeCardWidth)
                        .aspectRatio(16f / 9f)
                        .clip(CardShape)
                        .background(Bone),
                )
            }
        }
    }
}

/** Collection page skeleton — hero + horizontal card row. */
@Composable
fun CollectionShimmerSkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Bone),
        )
        Row(
            modifier = Modifier
                .padding(top = 56.dp, start = padding.start)
                .height(CardHeight),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(BrewLandscapeCardWidth)
                        .aspectRatio(16f / 9f)
                        .clip(CardShape)
                        .background(Bone),
                )
            }
        }
    }
}

@Composable
fun DetailsShimmerSkeleton(
    modifier: Modifier = Modifier,
    posterUri: String? = null,
) {
    val padding = rememberChildPadding()

    // Shared shimmer — one infinite transition driving a fraction 0→1
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
        ),
        label = "shimmerFraction",
    )

    // Per-block visibility — hero only; bottom trays are static
    val delays = listOf(0)
    val alphas = delays.map { delayMs ->
        val anim = remember(delayMs) { androidx.compose.animation.core.Animatable(0f) }
        LaunchedEffect(delayMs) {
            kotlinx.coroutines.delay(delayMs.toLong())
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(320, easing = FastOutSlowInEasing),
            )
        }
        anim.value
    }
    val offsets = delays.map { delayMs ->
        val anim = remember(delayMs) { androidx.compose.animation.core.Animatable(32f) }
        LaunchedEffect(delayMs) {
            kotlinx.coroutines.delay(delayMs.toLong())
            anim.animateTo(
                targetValue = 0f,
                animationSpec = tween(380, easing = FastOutSlowInEasing),
            )
        }
        anim.value
    }

    fun shimmerBrush(width: Float): androidx.compose.ui.graphics.Brush {
        val start = (shimmerFraction * (width + 600f)) - 300f
        return Brush.linearGradient(
            colors = listOf(Color(0xFF1C1C1C), Color(0xFF2E2E2E), Color(0xFF1C1C1C)),
            start = Offset(start, 0f),
            end = Offset(start + 400f, 0f),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // BLOCK 0 — Hero skeleton: same backdrop layout as MovieDetailShowcaseBackdrop
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(MovieDetailTokens.DetailShowcaseHeight)
                .graphicsLayer {
                    this.alpha = alphas[0]
                    translationY = offsets[0].dp.toPx()
                },
        ) {
            val w = constraints.maxWidth.toFloat()
            val shimmer = shimmerBrush(w)
            Box(modifier = Modifier.fillMaxSize()) {
                ShowcaseHeroShimmerBackdrop(
                    modifier = Modifier.fillMaxSize(),
                    backdropHeightFraction = 0.92f,
                    posterUri = posterUri,
                    posterFill = shimmer,
                )
                Image(
                    painter = painterResource(R.drawable.brew_logo),
                    contentDescription = "Brew",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 28.dp, end = padding.end)
                        .height(30.dp)
                        .width(78.dp),
                    contentScale = ContentScale.Fit,
                )
                Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(
                        start = padding.start,
                        end = padding.end,
                        bottom = 4.dp,
                    ),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(0.54f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmerBrush(w)),
                    )
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush(w)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush(w)),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(MovieDetailTokens.CtaFixedWidth)
                                .height(MovieDetailTokens.CtaMinHeight)
                                .clip(RoundedCornerShape(MovieDetailTokens.CtaWideRadius))
                                .background(shimmer),
                        )
                        Box(
                            modifier = Modifier
                                .width(MovieDetailTokens.CtaFixedWidth)
                                .height(MovieDetailTokens.CtaMinHeight)
                                .clip(RoundedCornerShape(MovieDetailTokens.CtaWideRadius))
                                .background(shimmer),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(MovieDetailTokens.SecondaryActionSize)
                                    .clip(RoundedCornerShape(MovieDetailTokens.SecondaryActionSize / 2))
                                    .background(shimmer),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.46f)
                        .padding(start = 12.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(shimmerBrush(w)),
                        )
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(shimmerBrush(w)),
                        )
                    }
                }
            }
            }
        }

        // BLOCK 1 — Customers also watched tray
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding.start)
                .padding(top = 16.dp),
        ) {
            val w = constraints.maxWidth.toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(160.dp).height(15.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush(w)))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .width(BrewLandscapeCardWidth)
                                .height(BrewLandscapeCardWidth * 9f / 16f)
                                .clip(CardShape)
                                .background(shimmerBrush(w)),
                        )
                    }
                }
            }
        }

        // BLOCK 2 — Reviews row
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding.start)
                .padding(top = 16.dp),
        ) {
            val w = constraints.maxWidth.toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(120.dp).height(15.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush(w)))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) {
                        Box(modifier = Modifier.width(200.dp).height(100.dp).clip(RoundedCornerShape(12.dp)).background(shimmerBrush(w)))
                    }
                }
            }
        }
    }
}
