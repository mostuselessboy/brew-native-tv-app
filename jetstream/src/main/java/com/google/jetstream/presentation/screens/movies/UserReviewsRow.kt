package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieReviewSummary
import com.google.jetstream.data.entities.MovieReviewsAndRatings
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.data.util.formatTimeAgo
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import java.util.Locale
import kotlin.math.round

private val ReviewCardBg = Color(0xFF111111)
private val ReviewCardShape = RoundedCornerShape(12.dp)
private val StirYellow = Color(0xFFFFC15E)
private val ReviewCardWidth = 320.dp
private val ReviewCardHeight = 164.dp
private val ReviewAvatarSize = 48.dp
private val VerifiedBadgeSize = 16.dp

/** Audience reviews — horizontal cards, no country split or distribution chart. */
@Composable
fun UserReviewsRow(
    reviews: List<MovieReviewsAndRatings>,
    summary: MovieReviewSummary?,
    userCountry: String,
    modifier: Modifier = Modifier,
) {
    if (reviews.isEmpty()) return
    val childPadding = rememberChildPadding()

    Column(modifier = modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MovieDetailSectionTitle(text = "Reviews")
            summary?.totalRatings?.takeIf { it > 0 }?.let { total ->
                Text(
                    text = "($total reviews)",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = childPadding.end),
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = reviews,
                key = { review ->
                    review.id.ifBlank { "${review.reviewerName}_${review.createdAt}_${review.reviewHeading}" }
                },
            ) { review ->
                UserReviewCard(review = review)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UserReviewCard(review: MovieReviewsAndRatings) {
    var expanded by remember { mutableStateOf(false) }
    val bodyText = review.reviewBody.trim()
    val showReadMore = bodyText.length > 120 && !expanded

    var isFocused by remember { mutableStateOf(false) }

    val scaleAnimationSpec = if (isFocused) {
        spring<Float>(
            dampingRatio = 0.85f,
            stiffness = 180f
        )
    } else {
        tween<Float>(durationMillis = 650, easing = LinearOutSlowInEasing)
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.10f else 1.0f,
        animationSpec = scaleAnimationSpec,
        label = "ReviewCardScale"
    )

    Surface(
        onClick = {
            if (bodyText.length > 120) expanded = !expanded
        },
        shape = ClickableSurfaceDefaults.shape(ReviewCardShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = ReviewCardShape,
            ),
            focusedBorder = Border(
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                shape = ReviewCardShape,
            ),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = Color.White.copy(alpha = 0.30f),
                elevation = 20.dp,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = ReviewCardBg,
            focusedContainerColor = ReviewCardBg,
        ),
        modifier = Modifier
            .width(ReviewCardWidth)
            .height(ReviewCardHeight)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .zIndex(if (isFocused) 10f else 1f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ReviewCardHeight)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ReviewAvatar(review = review)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(ReviewCardHeight - 20.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = review.reviewerName,
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    review.reviewRating?.takeIf { it > 0 }?.let { rating ->
                        CompactStarRating(rating = rating)
                    }
                    if (review.createdAt.isNotBlank()) {
                        Text(
                            text = formatTimeAgo(review.createdAt).uppercase(Locale.US),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                        )
                    }
                }
                if (review.reviewHeading.isNotBlank()) {
                    Text(
                        text = review.reviewHeading,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        letterSpacing = (-0.4).sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 5.dp),
                    )
                }
                Text(
                    text = bodyText,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ReviewAvatar(review: MovieReviewsAndRatings) {
    val context = LocalContext.current
    val avatarUrl = review.reviewerIconUri.takeIf { it.isNotBlank() }

    Box(
        modifier = Modifier
            .size(ReviewAvatarSize)
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.TopStart,
    ) {
        Box(
            modifier = Modifier
                .size(ReviewAvatarSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(BrewImageUrl.withDimensions(avatarUrl, 72, 72, quality = "100"))
                        .size(72, 72)
                        .crossfade(true)
                        .build(),
                    contentDescription = review.reviewerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(ReviewAvatarSize)
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = review.reviewerName.take(1).uppercase(Locale.US),
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
        if (review.isVerifiedCritic) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                tint = StirYellow,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(VerifiedBadgeSize),
            )
        }
    }
}

@Composable
private fun CompactStarRating(rating: Double) {
    val fillRating = snapOutOf5ForStarFill(rating)
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { index ->
            val starIndex = index + 1
            val filled = fillRating >= starIndex - 0.25
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (filled) StirYellow else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(9.dp),
            )
        }
    }
}

private fun snapOutOf5ForStarFill(rating: Double): Double {
    if (rating <= 0) return 0.0
    return (round(rating * 2) / 2.0).coerceIn(0.0, 5.0)
}
