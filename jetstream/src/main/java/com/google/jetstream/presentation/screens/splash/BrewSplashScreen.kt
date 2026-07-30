package com.google.jetstream.presentation.screens.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.util.BrewImageUrl

private const val WatchHiddenGemsUrl =
    "https://createstir.b-cdn.net/stir-static/watch-hidden-gems.png"

private val SplashBlack = Color(0xFF000000)
private val SeparatorGray = Color(0xFF5C5C5C)
private val BarTrack = Color(0xFF2A2A2A)
private val BarFill = Color.White

@Composable
fun BrewSplashScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shimmer by rememberInfiniteTransition(label = "splashBar").animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splashShimmer",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBlack),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.brew_logo),
                    contentDescription = "Brew",
                    modifier = Modifier
                        .height(36.dp)
                        .width(56.dp),
                    contentScale = ContentScale.Fit,
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(SeparatorGray),
                )

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(BrewImageUrl.forWatchHiddenGems(WatchHiddenGemsUrl))
                        .size(
                            BrewImageUrl.WATCH_HIDDEN_GEMS_WIDTH,
                            BrewImageUrl.WATCH_HIDDEN_GEMS_HEIGHT,
                        )
                        .crossfade(false)
                        .build(),
                    contentDescription = "Watch hidden gems",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(44.dp)
                        .width(88.dp),
                )
            }

            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BarTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(shimmer)
                        .clip(RoundedCornerShape(50))
                        .background(BarFill),
                )
            }
        }
    }
}
