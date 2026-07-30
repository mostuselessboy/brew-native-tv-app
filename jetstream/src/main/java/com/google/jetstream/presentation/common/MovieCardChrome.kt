package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.VodTagBadge
import com.google.jetstream.R

private val StoreIconGold = Color(0xFFE6D391)
private val LeavingSoonYellow = Color(0xFFFBBF24)

private const val BrewPlusWordmark =
    "https://createstir.b-cdn.net/stir-static/brew%2B.webp"
private const val FestivalWreath =
    "https://createstir.b-cdn.net/stir-static/wreath.png"

enum class BadgeVariant { White, Yellow }

/** Sales pitch first; else year • country — vod-frontend MovieCardInfoOverlay. */
fun movieCardMetaLine(movie: Movie): String {
    if (movie.description.isNotBlank()) return movie.description
    return listOfNotNull(movie.year, movie.country)
        .filter { it.isNotBlank() }
        .joinToString(" • ")
}

/** Showcase meta: genre • year • duration — vod-frontend buildShowcaseInfoLine. */
fun showcaseInfoLine(movie: Movie): String {
    return listOfNotNull(
        movie.genres.firstOrNull(),
        movie.year,
        movie.duration,
    ).filter { it.isNotBlank() }.joinToString("  •  ")
}

/** Top-left Store / Brew Plus + top-right vod_tag — cards only (not showcase). */
@Composable
fun MovieBadgeChrome(
    movie: Movie,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (compact) {
        Box(modifier = modifier.fillMaxSize()) {
            CommerceChromeRow(
                showStore = movie.showStore,
                showBrewPlus = movie.showBrewPlus,
                compact = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
            )

            if (movie.leavingSoon) {
                StatusBadge(
                    label = "Leaving soon",
                    variant = BadgeVariant.Yellow,
                    compact = true,
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }

            movie.vodTagLabel?.let { label ->
                StatusBadge(
                    label = label,
                    isFestival = movie.isFestivalTag || VodTagBadge.isFestivalStyle(label),
                    compact = true,
                    modifier = Modifier.align(Alignment.TopEnd),
                    positionEnd = true,
                )
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            CommerceChromeRow(
                showStore = movie.showStore,
                showBrewPlus = movie.showBrewPlus,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
            )

            if (movie.leavingSoon) {
                StatusBadge(
                    label = "Leaving soon",
                    variant = BadgeVariant.Yellow,
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }

            movie.vodTagLabel?.let { label ->
                StatusBadge(
                    label = label,
                    isFestival = movie.isFestivalTag || VodTagBadge.isFestivalStyle(label),
                    modifier = Modifier.align(Alignment.TopEnd),
                    positionEnd = true,
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    label: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.White,
    isFestival: Boolean = false,
    positionEnd: Boolean = false,
    compact: Boolean = false,
) {
    val background = when (variant) {
        BadgeVariant.White -> Color.White
        BadgeVariant.Yellow -> LeavingSoonYellow
    }
    val content = Color.Black
    val corner = if (compact) 5.dp else 8.dp
    val shape = if (positionEnd) {
        RoundedCornerShape(bottomStart = corner)
    } else {
        RoundedCornerShape(bottomEnd = corner)
    }
    val hPad = if (compact) 6.dp else 10.dp
    val vPad = if (compact) 3.dp else 4.dp
    val fontSize = if (compact) 9.sp else 12.sp
    val iconSize = if (compact) 10.dp else 14.dp
    val wreathSize = if (compact) 12.dp else 16.dp

    Row(
        modifier = modifier
            .background(background, shape)
            .padding(horizontal = hPad, vertical = vPad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
    ) {
        when {
            isFestival -> {
                AsyncImage(
                    model = FestivalWreath,
                    contentDescription = null,
                    modifier = Modifier.size(wreathSize),
                    contentScale = ContentScale.Fit,
                )
            }
            else -> {
                badgeIcon(label)?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
        Text(
            text = label.uppercase(),
            color = content,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = if (compact) 72.dp else 120.dp),
        )
    }
}

private fun badgeIcon(label: String): ImageVector? = when (label.lowercase()) {
    "new", "new release" -> Icons.Default.AutoAwesome
    "trending", "trending now" -> Icons.AutoMirrored.Filled.TrendingUp
    "hot" -> Icons.Default.LocalFireDepartment
    else -> null
}

@Composable
fun CommerceChromeRow(
    showStore: Boolean,
    showBrewPlus: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (!showStore && !showBrewPlus) return

    val storeSize = if (compact) 18.dp else 26.dp
    val storeIcon = if (compact) 8.dp else 12.dp
    val plusHeight = if (compact) 20.dp else 28.dp
    val plusWordmarkHeight = if (compact) 10.dp else 14.dp
    val plusWordmarkWidth = if (compact) 44.dp else 58.dp
    val plusHPadding = if (compact) 6.dp else 9.dp

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showStore) {
            Box(
                modifier = Modifier
                    .size(storeSize)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_brew_store),
                    contentDescription = "Store",
                    tint = StoreIconGold,
                    modifier = Modifier.size(storeIcon),
                )
            }
        }
        if (showBrewPlus) {
            Box(
                modifier = Modifier
                    .height(plusHeight)
                    .background(Color.Black, RoundedCornerShape(plusHeight / 2))
                    .padding(horizontal = plusHPadding),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(BrewPlusWordmark)
                        .size(if (compact) 180 else 240, if (compact) 40 else 52)
                        .build(),
                    contentDescription = "Brew Plus",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(plusWordmarkWidth)
                        .height(plusWordmarkHeight),
                )
            }
        }
    }
}
