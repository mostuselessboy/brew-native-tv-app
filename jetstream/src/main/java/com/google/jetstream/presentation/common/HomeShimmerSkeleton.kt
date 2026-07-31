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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = padding.start)
                .fillMaxWidth()
                .height(ShowcaseHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(Bone),
        )
        Column(
            modifier = Modifier.padding(horizontal = padding.start),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(148.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Bone),
                )
                Box(
                    modifier = Modifier
                        .width(148.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Bone),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Bone),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(start = padding.start),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Bone),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(118.dp)
                            .height(168.dp)
                            .clip(CardShape)
                            .background(Bone),
                    )
                }
            }
        }
    }
}
