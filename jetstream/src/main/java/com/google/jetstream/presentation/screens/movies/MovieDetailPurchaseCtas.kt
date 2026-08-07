package com.google.jetstream.presentation.screens.movies

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.DetailCtaColor
import com.google.jetstream.data.util.DetailCtaKind
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.data.util.DetailPurchaseCtaSlot
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.utils.suppressBringIntoViewOnFocus

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieDetailPurchaseCtaRow(
    movie: MovieDetails,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    primaryFocusRequester: FocusRequester? = null,
    reminderSet: Boolean = false,
) {
    val slots = DetailPurchaseCta.primaryRowSlots(movie).map { slot ->
        if (
            reminderSet &&
            (slot.kind == DetailCtaKind.ComingSoonNotify || slot.kind == DetailCtaKind.ComingSoon)
        ) {
            slot.copy(sublabel = "Reminder set")
        } else {
            slot
        }
    }

    if (slots.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .width(MovieDetailTokens.CtaFixedWidth)
            .graphicsLayer { clip = false }
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        slots.forEachIndexed { index, slot ->
            val isPrimary = index == 0
            if (slot.kind == DetailCtaKind.NotAvailable) {
                MovieDetailUnavailableCard(
                    slot = slot,
                    onPickRandom = onSecondaryAction,
                    primaryFocusRequester = if (isPrimary) primaryFocusRequester else null,
                    modifier = Modifier.width(MovieDetailTokens.CtaFixedWidth),
                )
            } else {
                DetailPurchaseCtaButton(
                    slot = slot,
                    compact = false,
                    onClick = if (isPrimary) onPrimaryAction else onSecondaryAction,
                    modifier = Modifier
                        .width(MovieDetailTokens.CtaFixedWidth)
                        .then(
                            if (isPrimary && primaryFocusRequester != null) {
                                Modifier.focusRequester(primaryFocusRequester)
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailPurchaseCtaButton(
    slot: DetailPurchaseCtaSlot,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onYellow = slot.color == DetailCtaColor.Yellow
    val style = wideStackColors(onYellow)
    val minHeight = when {
        slot.showBrewPlusLogo && compact -> MovieDetailTokens.CtaHalfRowSubscribeMinHeight
        slot.showBrewPlusLogo -> MovieDetailTokens.CtaSubscribeMinHeight
        compact -> MovieDetailTokens.CtaHalfRowMinHeight
        else -> MovieDetailTokens.CtaMinHeight
    }
    val padTop = if (compact) MovieDetailTokens.CtaHalfRowPadTop else MovieDetailTokens.CtaPadTop
    val padBottom = if (compact) MovieDetailTokens.CtaHalfRowPadBottom else MovieDetailTokens.CtaPadBottom
    val titleSize = if (compact) MovieDetailTokens.CtaHalfRowTitleSize else MovieDetailTokens.CtaTitleSize
    val titleLine = if (compact) MovieDetailTokens.CtaHalfRowTitleLine else MovieDetailTokens.CtaTitleLine
    val sublabelSize = if (compact) MovieDetailTokens.CtaHalfRowSublabelSize else MovieDetailTokens.CtaSublabelSize
    val sublabelLine = if (compact) MovieDetailTokens.CtaHalfRowSublabelLine else MovieDetailTokens.CtaSublabelLine
    val iconBoxSize = if (compact) MovieDetailTokens.CtaHalfRowIconBox else MovieDetailTokens.CtaIconBox
    val priceSize = if (compact) MovieDetailTokens.CtaHalfRowPriceSize else MovieDetailTokens.CtaPriceSize
    val cornerRadius = if (compact) MovieDetailTokens.CtaHalfRowRadius else MovieDetailTokens.CtaWideRadius
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
        label = "ctaScale",
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = minHeight)
            .suppressBringIntoViewOnFocus()
            .shadow(
                elevation = if (focused) 8.dp else 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.White.copy(alpha = 0.14f),
                spotColor = Color.White.copy(alpha = 0.22f),
                clip = false,
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                clip = false
            }
            .onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(cornerRadius)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.Transparent),
                shape = RoundedCornerShape(cornerRadius),
            ),
            focusedBorder = Border(
                border = BorderStroke(
                    2.dp,
                    if (onYellow) {
                        MovieDetailTokens.CtaFocusBorderYellow
                    } else {
                        MovieDetailTokens.CtaFocusBorderGray
                    },
                ),
                shape = RoundedCornerShape(cornerRadius),
            ),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = if (onYellow) {
                    MovieDetailTokens.AccentYellow.copy(alpha = 0.34f)
                } else {
                    Color.White.copy(alpha = 0.28f)
                },
                elevation = 18.dp,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = style.background,
            focusedContainerColor = style.background,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MovieDetailTokens.CtaPadH,
                        end = MovieDetailTokens.CtaPadH,
                        top = padTop,
                        bottom = padBottom,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 2.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = if (slot.showBrewPlusLogo) "${slot.title} Brew+" else slot.title,
                        color = style.text,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleSize,
                        lineHeight = titleLine,
                        letterSpacing = (-0.4).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                    )
                    slot.sublabel?.takeIf { it.isNotBlank() }?.let { sub ->
                        Text(
                            text = sub,
                            color = style.sublabel,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.Medium,
                            fontSize = sublabelSize,
                            lineHeight = sublabelLine,
                            letterSpacing = (-0.2).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }

                when {
                    slot.price != null -> {
                        PaidPriceColumn(
                            price = slot.price,
                            originalPrice = slot.originalPrice,
                            intervalSuffix = slot.intervalSuffix,
                            textColor = style.text,
                            sublabelColor = style.sublabel,
                            priceSize = priceSize,
                            compact = compact,
                        )
                    }
                    shouldShowRightIcon(slot) -> {
                        CtaIconBox(
                            background = style.iconBoxBg,
                            kind = slot.kind,
                            iconColor = Color.Black,
                            boxSize = iconBoxSize,
                            compact = compact,
                        )
                    }
                }
            }

            if (slot.progressPercent > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0x1A000000)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(slot.progressPercent / 100f)
                            .background(style.seekFill),
                    )
                }
            }
        }
    }
}

/** Matches mobile `PurchaseCtaApiStack` right-slot rules. */
private fun shouldShowRightIcon(slot: DetailPurchaseCtaSlot): Boolean {
    if (slot.price != null) return false
    return when (slot.kind) {
        DetailCtaKind.WatchForFree,
        DetailCtaKind.WatchNow,
        DetailCtaKind.ContinueWatching,
        DetailCtaKind.ComingSoon,
        DetailCtaKind.ComingSoonNotify,
        DetailCtaKind.Rent,
        DetailCtaKind.Buy,
        DetailCtaKind.SupportFilmmaker -> true
        DetailCtaKind.NotAvailable,
        DetailCtaKind.SubscribeYearly,
        DetailCtaKind.SubscribeQuarterly -> false
        else -> false
    }
}

@Composable
private fun CtaIconBox(
    background: Color,
    kind: DetailCtaKind,
    iconColor: Color,
    boxSize: androidx.compose.ui.unit.Dp,
    compact: Boolean,
) {
    val iconSize = when (kind) {
        DetailCtaKind.Rent -> if (compact) 18.dp else 20.dp
        DetailCtaKind.ComingSoon,
        DetailCtaKind.ComingSoonNotify -> if (compact) 16.dp else 18.dp
        else -> if (compact) 15.dp else 16.dp
    }
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(MovieDetailTokens.CtaIconBoxRadius))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            DetailCtaKind.Rent -> Icon(
                painter = painterResource(R.drawable.ic_mdi_ticket_confirmation),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
            DetailCtaKind.ComingSoon,
            DetailCtaKind.ComingSoonNotify -> Icon(
                painter = painterResource(R.drawable.ic_lucide_bell),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
            DetailCtaKind.SupportFilmmaker -> Icon(
                painter = painterResource(R.drawable.ic_clap),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
            else -> Icon(
                painter = painterResource(R.drawable.ic_fa_play),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(iconSize)
                    .padding(start = if (compact) 1.dp else 2.dp),
            )
        }
    }
}

@Composable
private fun PaidPriceColumn(
    price: String,
    originalPrice: String?,
    intervalSuffix: String?,
    textColor: Color,
    sublabelColor: Color,
    priceSize: androidx.compose.ui.unit.TextUnit,
    compact: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.width(
            if (compact) MovieDetailTokens.CtaHalfRowPriceColumnWidth
            else MovieDetailTokens.CtaPriceColumnWidth,
        ),
    ) {
        Text(
            text = buildString {
                append(price)
                intervalSuffix?.let { append(it) }
            },
            color = textColor,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = priceSize,
            lineHeight = if (compact) 18.sp else 20.sp,
            letterSpacing = (-0.4).sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        originalPrice?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                color = sublabelColor,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Medium,
                fontSize = if (compact) 10.sp else 11.sp,
                lineHeight = if (compact) 12.sp else 13.sp,
                textDecoration = TextDecoration.LineThrough,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailPurchaseCtaSkeletonButton(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Surface(
        onClick = {},
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF2A2A2A).copy(alpha = alpha),
            contentColor = Color.Transparent,
            focusedContainerColor = Color(0xFF4A4A4A),
            focusedContentColor = Color.Transparent,
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = Color(0xFFFF9A4D).copy(alpha = 0.2f),
                elevation = 8.dp
            )
        ),
        modifier = modifier
            .height(MovieDetailTokens.CtaMinHeight)
            .suppressBringIntoViewOnFocus()
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun MovieDetailSkeletonCtaRow(
    modifier: Modifier = Modifier,
    primaryFocusRequester: FocusRequester? = null,
) {
    Column(
        modifier = modifier
            .width(MovieDetailTokens.CtaFixedWidth)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DetailPurchaseCtaSkeletonButton(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (primaryFocusRequester != null) {
                        Modifier.focusRequester(primaryFocusRequester)
                    } else {
                        Modifier
                    }
                )
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MovieDetailUnavailableCard(
    slot: DetailPurchaseCtaSlot,
    onPickRandom: () -> Unit,
    primaryFocusRequester: FocusRequester?,
    modifier: Modifier = Modifier,
) {
    val message = slot.sublabel ?: "This film isn't available in your country, but many other great films are."

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = (-0.2).sp,
            )

            Surface(
                onClick = onPickRandom,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White,
                    focusedContainerColor = Color.White,
                    contentColor = Color.Black,
                    focusedContentColor = Color.Black,
                ),
                border = ClickableSurfaceDefaults.border(
                    border = Border(BorderStroke(1.dp, Color.Transparent), shape = RoundedCornerShape(50.dp)),
                    focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = RoundedCornerShape(50.dp)),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .then(
                        if (primaryFocusRequester != null) Modifier.focusRequester(primaryFocusRequester)
                        else Modifier,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Pick one for me ",
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                    )
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.dice_brew),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    )
                }
            }
        }
    }
}

