package com.google.jetstream.presentation.common

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.common.ShowcaseHeight

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


@Composable
fun DetailsShimmerSkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()

    // Slide-up + fade entrance
    val offsetY = remember { androidx.compose.animation.core.Animatable(60f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.coroutineScope {
            launch {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.tween(480, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(380),
                )
            }
        }
    }

    // Shimmer sweep
    val shimmerColors = listOf(
        Color(0xFF1A1A1A),
        Color(0xFF2C2C2C),
        Color(0xFF1A1A1A),
    )
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.LinearEasing),
        ),
        label = "shimmerX",
    )
    val shimmerBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(shimmerX, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerX + 600f, 0f),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer {
                translationY = offsetY.value.dp.toPx()
                this.alpha = alpha.value
            },
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Hero backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShowcaseHeight)
                .background(shimmerBrush),
        )

        Column(
            modifier = Modifier.padding(horizontal = padding.start).padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Title block
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush),
            )
            // Tagline
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
            // Meta info line
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
            // CTA buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(152.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmerBrush),
                )
                Box(
                    modifier = Modifier
                        .width(152.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(shimmerBrush),
                )
            }
            // Secondary action icons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(shimmerBrush),
                    )
                }
            }
        }

        // Tray section 1 — customers also watched
        Column(
            modifier = Modifier.padding(start = padding.start).padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .width(BrewLandscapeCardWidth)
                            .height(BrewLandscapeCardWidth * 9f / 16f)
                            .clip(CardShape)
                            .background(shimmerBrush),
                    )
                }
            }
        }

        // Tray section 2 — cast
        Column(
            modifier = Modifier.padding(start = padding.start).padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(6) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(shimmerBrush),
                        )
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush),
                        )
                    }
                }
            }
        }
    }
}
