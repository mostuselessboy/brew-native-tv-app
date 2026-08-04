package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.google.jetstream.presentation.theme.BrewTitle
import java.util.Locale
import kotlin.math.round

private val StarEmpty = Color.White.copy(alpha = 0.18f)
private val StarFilled = MovieDetailTokens.AccentYellow

/**
 * Hero star row — half-star aware, tighter than Material defaults.
 */
@Composable
fun MovieDetailStarRating(
    averageRating: Double,
    ratingCount: Int,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
    starSize: Dp = 15.dp,
) {
    val normalized = if (averageRating > 5) averageRating / 2.0 else averageRating
    val fillRating = snapOutOf5ForStarFill(normalized)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            BrewRatingStar(
                fillAmount = (fillRating - index).coerceIn(0.0, 1.0),
                size = starSize,
            )
        }
        if (showCount && normalized > 0) {
            Text(
                text = buildString {
                    append(String.format(Locale.US, "%.1f/5", normalized))
                    val count = ratingCount
                    if (count > 0) {
                        append(" (")
                        append(String.format(Locale.US, "%,d", count))
                        append(" ratings)")
                    }
                },
                color = MovieDetailTokens.White90,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = (-0.2).sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
fun BrewStarRatingRow(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 12.dp,
    spacing: Dp = 2.dp,
) {
    val normalized = if (rating > 5) rating / 2.0 else rating
    val fillRating = snapOutOf5ForStarFill(normalized)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(5) { index ->
            BrewRatingStar(
                fillAmount = (fillRating - index).coerceIn(0.0, 1.0),
                size = starSize,
            )
        }
    }
}

@Composable
private fun BrewRatingStar(
    fillAmount: Double,
    size: Dp,
) {
    Box(
        modifier = Modifier
            .padding(end = 2.dp)
            .size(size),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = StarEmpty,
            modifier = Modifier.size(size),
        )
        if (fillAmount > 0) {
            Box(
                modifier = Modifier
                    .width(size * fillAmount.toFloat())
                    .fillMaxHeight()
                    .clip(androidx.compose.ui.graphics.RectangleShape),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarFilled,
                    modifier = Modifier.size(size),
                )
            }
        }
    }
}

private fun snapOutOf5ForStarFill(rating: Double): Double {
    if (rating <= 0) return 0.0
    val snapped = round(rating * 2) / 2.0
    return snapped.coerceIn(0.0, 5.0)
}
