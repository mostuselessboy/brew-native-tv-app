package com.google.jetstream.presentation.screens.movies

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Port of mobile-viewer `purchase-ctas/tokens.ts` — wide-stack CTA dimensions. */
internal object MovieDetailTokens {
    val Canvas = Color(0xFF000000)
    val AccentYellow = Color(0xFFFFC15E)
    val White90 = Color(0xE6FFFFFF)
    val White70 = Color(0xB3FFFFFF)
    val White60 = Color(0x99FFFFFF)
    val SecondaryFill = Color(0x1AFFFFFF)
    val SecondaryBorder = Color(0x1FFFFFFF)
    val SecondaryActionBg = Color(0x0DFFFFFF)

    /** `CTA_WIDE_BORDER_RADIUS` — mobile uses 12; TV half-rows use 10 to avoid pill shape. */
    val CtaWideRadius = 8.dp
    val CtaHalfRowRadius = 8.dp
    val CtaIconBoxRadius = 12.dp

    val CtaFixedWidth = 210.dp
    val CtaHalfRowFixedWidth = 210.dp

    val CtaMinHeight = 38.dp
    val CtaHalfRowMinHeight = 36.dp
    val CtaSubscribeMinHeight = 38.dp
    val CtaHalfRowSubscribeMinHeight = 36.dp

    val CtaPadH = 10.dp
    val CtaPadTop = 12.dp
    val CtaPadBottom = 10.dp
    val CtaHalfRowPadTop = 12.dp
    val CtaHalfRowPadBottom = 10.dp

    val CtaIconBox = 26.dp
    val CtaHalfRowIconBox = 30.dp

    val CtaTitleSize = 14.sp
    val CtaTitleLine = 17.sp
    val CtaHalfRowTitleSize = 14.sp
    val CtaHalfRowTitleLine = 17.sp

    val CtaSublabelSize = 9.sp
    val CtaSublabelLine = 12.sp
    val CtaHalfRowSublabelSize = 8.sp
    val CtaHalfRowSublabelLine = 11.sp

    val CtaPriceSize = 13.sp
    val CtaHalfRowPriceSize = 12.sp
    val CtaPriceColumnWidth = 80.dp
    val CtaHalfRowPriceColumnWidth = 70.dp

    /** Stacked subscribe wordmark — sized to fit CTA row without dominating. */
    val BrewPlusLogoWidth = 52.dp
    val BrewPlusLogoCompactWidth = 44.dp

    val SecondaryActionSize = 36.dp
    val SecondaryActionBorder = Color.White.copy(alpha = 0.22f)

    val CtaFocusBorderGray = Color(0xFF9CA3AF)
    val CtaFocusBorderYellow = Color(0xFFB8860B)

    val HeroTitleSize = 56.sp
    val HeroTitleLine = 52.sp
    val HeroBottomPadding = 28.dp
    val HeroViewportFraction = 0.74f
    const val BackdropParallaxFactor = 0.48f
    const val BackdropScale = 1.08f
    val TaglineSize = 16.sp
    val SynopsisSize = 10.sp
    val MetaSize = 12.sp
    val DetailShowcaseHeight = 410.dp
    val SectionTitleSize = 17.sp
    val SectionTitleColor = Color(0xFFD1D5DB)

    val YellowTextPrimary = Color.Black
    val YellowTextSecondary = Color(0xB3000000)
    val WhiteTextPrimary = Color.Black
    val WhiteTextSecondary = Color(0x8C000000)
    val YellowSeekFill = Color(0xFFB8860B)
    val IconBoxBgYellow = Color(0x1A000000)
    val IconBoxBgWhite = Color(0x14000000)

    val CheckGreen = Color(0xFF4ADE80)
    val XGray = Color(0xFF9CA3AF)
}

internal fun wideStackColors(yellow: Boolean): WideStackStyle = if (yellow) {
    WideStackStyle(
        background = MovieDetailTokens.AccentYellow,
        text = MovieDetailTokens.YellowTextPrimary,
        sublabel = MovieDetailTokens.YellowTextSecondary,
        iconBoxBg = MovieDetailTokens.IconBoxBgYellow,
        seekFill = MovieDetailTokens.YellowSeekFill,
    )
} else {
    WideStackStyle(
        background = Color.White,
        text = MovieDetailTokens.WhiteTextPrimary,
        sublabel = MovieDetailTokens.WhiteTextSecondary,
        iconBoxBg = MovieDetailTokens.IconBoxBgWhite,
        seekFill = MovieDetailTokens.AccentYellow,
    )
}

internal data class WideStackStyle(
    val background: Color,
    val text: Color,
    val sublabel: Color,
    val iconBoxBg: Color,
    val seekFill: Color,
)
