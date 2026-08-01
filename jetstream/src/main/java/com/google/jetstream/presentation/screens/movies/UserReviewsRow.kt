package com.google.jetstream.presentation.screens.movies

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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

private val ReviewCardBg = Color(0xFF111111)
private val ReviewCardShape = RoundedCornerShape(16.dp)
private val StirYellow = Color(0xFFFFC15E)
private val ReviewCardWidth = 280.dp
private val ReviewCardHeight = 188.dp
private val SummaryCardWidth = 200.dp
private val SummaryCardHeight = 188.dp
private val ReviewAvatarSize = 26.dp
private val VerifiedBadgeSize = 10.dp

/** Audience reviews — summary hero card + center-aligned review cards. */
@Composable
fun UserReviewsRow(
    reviews: List<MovieReviewsAndRatings>,
    summary: MovieReviewSummary?,
    userCountry: String,
    modifier: Modifier = Modifier,
) {
    val hasSummary = summary != null && (
        summary.averageRating > 0 || summary.totalRatings > 0
    )
    if (reviews.isEmpty() && !hasSummary) return
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (hasSummary) {
                item(key = "review_summary") {
                    UserReviewSummaryCard(summary = summary!!)
                }
            }
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

@Composable
private fun UserReviewSummaryCard(summary: MovieReviewSummary) {
    val normalized = if (summary.averageRating > 5) summary.averageRating / 2.0 else summary.averageRating

    Box(
        modifier = Modifier
            .width(SummaryCardWidth)
            .height(SummaryCardHeight)
            .clip(ReviewCardShape)
            .background(ReviewCardBg)
            .focusProperties { canFocus = false },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(SummaryCardHeight)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (normalized > 0) {
                    String.format(Locale.US, "%.1f", normalized)
                } else {
                    "—"
                },
                color = StirYellow,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = 48.sp,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center,
            )
            if (normalized > 0) {
                BrewStarRatingRow(
                    rating = normalized,
                    starSize = 16.dp,
                    spacing = 3.dp,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            Text(
                text = "out of 5",
                color = Color.White.copy(alpha = 0.38f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.3.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            summary.totalRatings.takeIf { it > 0 }?.let { total ->
                Text(
                    text = String.format(Locale.US, "%,d ratings", total),
                    color = Color.White.copy(alpha = 0.55f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = (-0.15).sp,
                    modifier = Modifier.padding(top = 10.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UserReviewCard(review: MovieReviewsAndRatings) {
    var expanded by remember { mutableStateOf(false) }
    val bodyText = review.reviewBody.trim()
    val showReadMore = bodyText.length > 140 && !expanded
    var isFocused by remember { mutableStateOf(false) }

    val scaleAnimationSpec = if (isFocused) {
        spring<Float>(dampingRatio = 0.85f, stiffness = 180f)
    } else {
        tween<Float>(durationMillis = 650, easing = LinearOutSlowInEasing)
    }
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = scaleAnimationSpec,
        label = "ReviewCardScale",
    )

    Surface(
        onClick = {
            if (bodyText.length > 140) expanded = !expanded
        },
        shape = ClickableSurfaceDefaults.shape(ReviewCardShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                shape = ReviewCardShape,
            ),
            focusedBorder = Border(
                border = BorderStroke(1.5.dp, Color.White),
                shape = ReviewCardShape,
            ),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = Color.White.copy(alpha = 0.26f),
                elevation = 20.dp,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = ReviewCardBg,
            focusedContainerColor = Color(0xFF151515),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(ReviewCardHeight)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (review.reviewHeading.isNotBlank()) {
                    Text(
                        text = review.reviewHeading,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        letterSpacing = (-0.3).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
                review.reviewRating?.takeIf { it > 0 }?.let { rating ->
                    BrewStarRatingRow(
                        rating = rating,
                        starSize = 13.dp,
                        spacing = 2.dp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                if (bodyText.isNotBlank()) {
                    Text(
                        text = bodyText,
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        letterSpacing = (-0.1).sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            ReviewAuthorRow(review = review)
        }
    }
}

@Composable
private fun ReviewAuthorRow(review: MovieReviewsAndRatings) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReviewAvatar(review = review)
        Text(
            text = review.reviewerName,
            color = Color.White.copy(alpha = 0.82f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
        if (review.createdAt.isNotBlank()) {
            Text(
                text = "· ${formatTimeAgo(review.createdAt)}",
                color = Color.White.copy(alpha = 0.34f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun ReviewAvatar(review: MovieReviewsAndRatings) {
    val context = LocalContext.current
    val avatarUrl = review.reviewerIconUri.takeIf { it.isNotBlank() }

    Box(
        modifier = Modifier.size(ReviewAvatarSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ReviewAvatarSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
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
                    fontSize = 10.sp,
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
