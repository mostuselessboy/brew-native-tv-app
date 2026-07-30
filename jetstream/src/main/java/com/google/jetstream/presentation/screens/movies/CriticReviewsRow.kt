package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieCriticReview
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private val CardBg = Color(0xFF111111)
private val CardShape = RoundedCornerShape(16.dp)
private val CardWidth = 300.dp

/**
 * Critic review quote cards — mirrors vod-frontend `CriticReviews.tsx`.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CriticReviewsRow(
    reviews: List<MovieCriticReview>,
    modifier: Modifier = Modifier,
) {
    if (reviews.isEmpty()) return
    val childPadding = rememberChildPadding()

    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            text = "Critics' Reviews",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.padding(start = childPadding.start),
        )
        LazyRow(
            modifier = Modifier
                .padding(top = 14.dp)
                .focusRestorer(),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(reviews, key = { it.id }) { review ->
                CriticReviewCard(review = review)
            }
        }
    }
}

@Composable
private fun CriticReviewCard(review: MovieCriticReview) {
    val context = LocalContext.current
    Surface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(CardShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = CardShape,
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.85f)),
                shape = CardShape,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CardBg,
            focusedContainerColor = CardBg,
        ),
        modifier = Modifier.width(CardWidth),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (!review.orgLogoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(BrewImageUrl.forCriticLogo(review.orgLogoUrl))
                            .size(
                                BrewImageUrl.CRITIC_LOGO_WIDTH,
                                BrewImageUrl.CRITIC_LOGO_HEIGHT,
                            )
                            .crossfade(true)
                            .build(),
                        contentDescription = review.orgName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                    )
                }
            }

            Text(
                text = "\"${review.quote}\"",
                color = Color.White.copy(alpha = 0.9f),
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .height(44.dp),
            )

            Spacer(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 10.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.2f)),
            )

            Text(
                text = review.author.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!review.dateLabel.isNullOrBlank()) {
                Text(
                    text = review.dateLabel,
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
