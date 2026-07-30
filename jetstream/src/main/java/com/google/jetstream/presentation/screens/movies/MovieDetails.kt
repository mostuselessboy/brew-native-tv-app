package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.launch

/**
 * Prime-style detail hero: meta badges, synopsis + MORE, primary + icon action row.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieDetails(
    movieDetails: MovieDetails,
    goToMoviePlayer: () -> Unit,
    isLoggedIn: Boolean = false,
    onLoginRequired: () -> Unit = {},
) {
    val childPadding = rememberChildPadding()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var synopsisExpanded by remember(movieDetails.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .bringIntoViewRequester(bringIntoViewRequester),
    ) {
        MovieBackdrop(
            movieDetails = movieDetails,
            modifier = Modifier.fillMaxSize(),
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = childPadding.start, end = childPadding.end, bottom = 32.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp),
            ) {
                Text(
                    text = movieDetails.name,
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1.6).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                DetailMetaRow(
                    releaseLabel = movieDetails.releaseDate.take(4).takeIf { it.length == 4 }
                        ?: movieDetails.releaseDate.take(10),
                    genres = movieDetails.categories.take(2).joinToString(", "),
                    duration = movieDetails.duration,
                    ratingBadge = movieDetails.pgRating,
                    ratingSummary = movieDetails.revenue.takeIf { it.contains('★') },
                    modifier = Modifier.padding(top = 12.dp),
                )

                if (movieDetails.description.isNotBlank()) {
                    SynopsisWithMore(
                        text = movieDetails.description,
                        expanded = synopsisExpanded,
                        onToggle = { synopsisExpanded = !synopsisExpanded },
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }

                if (movieDetails.director != "—") {
                    Text(
                        text = "Directed by ${movieDetails.director}",
                        color = Color.White.copy(alpha = 0.58f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                DetailActionBar(
                    purchaseCta = movieDetails.purchaseCta,
                    hasTrailer = movieDetails.videoUri.isNotBlank(),
                    isLoggedIn = isLoggedIn,
                    onWatch = goToMoviePlayer,
                    onRent = onLoginRequired,
                    onBuy = onLoginRequired,
                    onSubscribe = onLoginRequired,
                    onLoginRequired = onLoginRequired,
                    onTrailer = goToMoviePlayer,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                            }
                        },
                )
            }

            if (movieDetails.posterUri.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(BrewImageUrl.forDetailPoster(movieDetails.posterUri))
                        .size(BrewImageUrl.DETAIL_POSTER_WIDTH, BrewImageUrl.DETAIL_POSTER_HEIGHT)
                        .crossfade(180)
                        .build(),
                    contentDescription = movieDetails.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(168.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }
        }
    }
}

@Composable
private fun SynopsisWithMore(
    text: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showMore = text.length > 100
    Column(modifier = modifier) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (showMore) {
            Text(
                text = if (expanded) "LESS" else "MORE",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .background(Color.White.copy(alpha = 0.14f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle,
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun MovieBackdrop(
    movieDetails: MovieDetails,
    modifier: Modifier = Modifier,
) {
    val scrim = Color(0xFF000000)
    val poster = movieDetails.posterUri
    Box(modifier = modifier.background(Color(0xFF111111))) {
        if (poster.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(BrewImageUrl.forDetail(poster))
                    .size(BrewImageUrl.DETAIL_WIDTH, BrewImageUrl.DETAIL_HEIGHT)
                    .crossfade(220)
                    .build(),
                contentDescription = StringConstants
                    .Composable
                    .ContentDescription
                    .moviePoster(movieDetails.name),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.1f to Color.Transparent,
                                    0.5f to scrim.copy(alpha = 0.3f),
                                    0.78f to scrim.copy(alpha = 0.85f),
                                    1f to scrim,
                                ),
                            ),
                        )
                        drawRect(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0f to scrim.copy(alpha = 0.82f),
                                    0.45f to scrim.copy(alpha = 0.25f),
                                    0.72f to Color.Transparent,
                                ),
                            ),
                        )
                    },
            )
        }
    }
}
