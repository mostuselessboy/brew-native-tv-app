package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.jetstream.R

private const val BrewPlusWordmarkAspect = 820f / 200f
private const val BrewPlusInlineScale = 1.35f

/** Inline or stacked Brew+ wordmark — parity with mobile `SubscribeCtaLogo`. */
@Composable
fun BrewPlusCtaLogo(
    onYellowBackground: Boolean,
    compact: Boolean = false,
    stacked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val baseWidth = when {
        stacked && compact -> MovieDetailTokens.BrewPlusLogoCompactWidth
        stacked -> MovieDetailTokens.BrewPlusLogoWidth
        compact -> MovieDetailTokens.BrewPlusLogoCompactWidth * 0.72f
        else -> MovieDetailTokens.BrewPlusLogoWidth * 0.72f
    }
    val logoWidth = if (stacked) baseWidth else baseWidth * BrewPlusInlineScale
    val logoHeight = logoWidth / BrewPlusWordmarkAspect

    Image(
        painter = painterResource(
            if (onYellowBackground) {
                R.drawable.brewplus_wordmark_black
            } else {
                R.drawable.brewplus_wordmark_gold
            },
        ),
        contentDescription = "Brew+",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .width(logoWidth)
            .height(logoHeight),
    )
}
