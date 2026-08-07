package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private const val BrewLogoCdnUrl = "https://createstir.b-cdn.net/stir-static/brew.png?w=240"
private const val WatchHiddenGemsCdnUrl = "https://createstir.b-cdn.net/stir-static/watch-hidden-gems.png?w=360"

@Composable
fun BrewBrandLogo(
    modifier: Modifier = Modifier,
    logoHeight: Dp = 28.dp,
) {
    val context = LocalContext.current
    val brewLogoReq = remember(context) {
        ImageRequest.Builder(context)
            .data(BrewLogoCdnUrl)
            .crossfade(false)
            .build()
    }
    val hiddenGemsReq = remember(context) {
        ImageRequest.Builder(context)
            .data(WatchHiddenGemsCdnUrl)
            .crossfade(false)
            .build()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = brewLogoReq,
            contentDescription = "Brew",
            modifier = Modifier.height(logoHeight),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
        )

        // Vertical divider line between Brew logo and Watch hidden gems logo
        Box(
            modifier = Modifier
                .height(logoHeight * 0.85f)
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.35f))
        )

        AsyncImage(
            model = hiddenGemsReq,
            contentDescription = "Watch Hidden Gems",
            modifier = Modifier.height(logoHeight),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
        )
    }
}
