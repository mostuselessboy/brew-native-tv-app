package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.screens.home.ShowcaseHeight

private val SkeletonBase = Color(0xFF1A1A1A)
private val SkeletonMuted = Color(0xFF242424)
private val CardShape = RoundedCornerShape(9.dp)

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CardShape,
    color: Color = SkeletonBase,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color),
    )
}

@Composable
fun HomeShimmerSkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShowcaseHeight)
                .padding(horizontal = padding.start),
            shape = RoundedCornerShape(10.dp),
        )

        repeat(3) {
            SkeletonTrayRow(startPadding = padding.start)
        }
    }
}

@Composable
private fun SkeletonTrayRow(
    startPadding: androidx.compose.ui.unit.Dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier
                .padding(start = startPadding)
                .width(160.dp)
                .height(16.dp),
            shape = RoundedCornerShape(5.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(4) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(BrewLandscapeCardWidth)
                        .aspectRatio(16f / 9f),
                )
            }
        }
    }
}

/** Static placeholder matching the movie-details hero layout — no shimmer overlap. */
@Composable
fun DetailsShimmerSkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .background(SkeletonMuted),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding.start)
                .padding(top = 36.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(38.dp),
                shape = RoundedCornerShape(6.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(52.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SkeletonMuted,
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(96.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SkeletonMuted,
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(72.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SkeletonMuted,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(13.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SkeletonMuted,
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(13.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SkeletonMuted,
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(13.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SkeletonMuted,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(176.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(128.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SkeletonMuted,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SkeletonBlock(
                modifier = Modifier
                    .width(168.dp)
                    .height(20.dp),
                shape = RoundedCornerShape(5.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) {
                    SkeletonBlock(
                        modifier = Modifier
                            .width(108.dp)
                            .aspectRatio(2f / 3f),
                    )
                }
            }
        }
    }
}
