package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

private val Bone = Color(0xFF141414)
private val CardShape = RoundedCornerShape(6.dp)

/** Static home skeleton — no animated shimmer (lighter on TV). */
@Composable
fun HomeShimmerSkeleton(modifier: Modifier = Modifier) {
    val padding = rememberChildPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShowcaseHeight)
                .padding(horizontal = padding.start)
                .clip(RoundedCornerShape(6.dp))
                .background(Bone),
        )

        repeat(2) {
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(start = startPadding)
                .width(140.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Bone),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .background(Bone),
        )
        Column(
            modifier = Modifier.padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(start = padding.start)
                    .width(200.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Bone),
            )
            Row(
                modifier = Modifier.padding(start = padding.start),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .width(132.dp)
                            .aspectRatio(3f / 4f)
                            .clip(CardShape)
                            .background(Bone),
                    )
                }
            }
        }
    }
}
