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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.MovieCriticReview
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewDisplay
import com.google.jetstream.presentation.theme.BrewTitle

private val CardShape = RoundedCornerShape(14.dp)
private val CardWidth = 220.dp
private val CardHeight = 190.dp
private val CardGradientTop = Color(0xFF1C1C1E)
private val CardGradientBottom = Color(0xFF0A0A0A)

/** Critic review cards — polished TV layout. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CriticReviewsRow(
    reviews: List<MovieCriticReview>,
    modifier: Modifier = Modifier,
    firstItemFocusRequester: FocusRequester? = null,
) {
    if (reviews.isEmpty()) return
    val childPadding = rememberChildPadding()

    Column(modifier = modifier.padding(top = 2.dp)) {
        MovieDetailSectionTitle(text = "Critics' Reviews")
        LazyRow(
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(reviews, key = { it.id }) { review ->
                val isFirst = review.id == reviews.first().id
                CriticReviewCard(
                    review = review,
                    modifier = Modifier
                        .then(
                            if (isFirst && firstItemFocusRequester != null) {
                                Modifier.focusRequester(firstItemFocusRequester)
                            } else {
                                Modifier
                            }
                        ),
                )
            }
        }
    }
}

@Composable
private fun CriticReviewCard(
    review: MovieCriticReview,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
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
        label = "CriticCardScale"
    )

    Surface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(CardShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = CardShape,
            ),
            focusedBorder = Border(
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                shape = CardShape,
            ),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = Color.White.copy(alpha = 0.50f),
                elevation = 20.dp,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
        ),
        modifier = modifier
            .width(CardWidth)
            .height(CardHeight)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .zIndex(if (isFocused) 10f else 1f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardGradientTop, CardGradientBottom),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardHeight)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(vertical = 6.dp, horizontal = 10.dp)
                        .height(28.dp),
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
                                .height(22.dp),
                        )
                    }
                }

                Text(
                    text = "\"${review.quote}\"",
                    color = Color.White.copy(alpha = 0.92f),
                    fontFamily = BrewDisplay,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 10.dp, bottom = 8.dp),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    AccentLine.copy(alpha = 0.45f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Text(
                    text = review.author.uppercase(),
                    color = Color.White.copy(alpha = 0.75f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!review.dateLabel.isNullOrBlank()) {
                        Text(
                            text = review.dateLabel,
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    } else {
                        Box(modifier = Modifier.width(1.dp))
                    }

                    if (!review.link.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = "Read article",
                                color = AccentLine.copy(alpha = 0.85f),
                                fontFamily = BrewTitle,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_external_link),
                                contentDescription = null,
                                tint = AccentLine.copy(alpha = 0.85f),
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
