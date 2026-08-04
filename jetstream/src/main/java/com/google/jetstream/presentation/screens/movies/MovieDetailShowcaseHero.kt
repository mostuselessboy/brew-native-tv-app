package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.google.jetstream.R
import kotlinx.coroutines.delay
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.presentation.common.RibbonLabelBadge
import com.google.jetstream.presentation.common.RibbonLabelBadgeSize
import com.google.jetstream.presentation.common.ShowcaseHeroBackdrop
import com.google.jetstream.presentation.common.ShowcaseHeroStyles
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private val StirYellow = MovieDetailTokens.AccentYellow

/** Backdrop inside the scrolling hero — dims as user scrolls down. */
@Composable
fun MovieDetailShowcaseBackdrop(
    movieDetails: MovieDetails,
    scrollDimAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9A4D).copy(alpha = 0.10f),
                            Color(0xFFFF7A2E).copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.72f,
                    ),
                )
            }
            .background(Color.Black),
    ) {
        ShowcaseHeroBackdrop(
            posterUri = movieDetails.posterUri,
            contentDescription = movieDetails.name,
            modifier = Modifier.fillMaxSize(),
            backdropHeightFraction = 0.92f,
        )
        if (scrollDimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrollDimAlpha)),
            )
        }
    }
}

@Composable
fun MovieDetailShowcaseContent(
    movieDetails: MovieDetails,
    onPrimaryCtaClick: () -> Unit,
    onSecondaryCtaClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onOpenLanguages: () -> Unit,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    purchaseLoading: Boolean = false,
    reminderSet: Boolean = false,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    primaryFocusRequester: FocusRequester? = null,
) {
    val childPadding = rememberChildPadding()
    val purchaseSlots = remember(movieDetails) { DetailPurchaseCta.primaryRowSlots(movieDetails) }
    val hasPurchaseCtas = purchaseSlots.isNotEmpty()
    val shortLine = movieDetails.tagline.takeIf { it.isNotBlank() }

    var wasPurchaseLoading by remember { mutableStateOf(purchaseLoading) }
    LaunchedEffect(purchaseLoading, hasPurchaseCtas) {
        val loadingJustFinished = wasPurchaseLoading && !purchaseLoading
        wasPurchaseLoading = purchaseLoading
        if (loadingJustFinished && hasPurchaseCtas && primaryFocusRequester != null) {
            delay(50)
            runCatching { primaryFocusRequester.requestFocus() }
        }
    }

    val averageRating = movieDetails.averageRating
        ?: movieDetails.reviewSummary?.averageRating
        ?: 0.0
    val ratingCount = movieDetails.ratingCount
        ?: movieDetails.reviewSummary?.totalRatings
        ?: 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MovieDetailTokens.DetailShowcaseHeight),
    ) {
        Image(
            painter = painterResource(R.drawable.brew_logo),
            contentDescription = "Brew",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = childPadding.end)
                .height(30.dp)
                .width(78.dp),
            contentScale = ContentScale.Fit,
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = childPadding.start,
                    end = childPadding.end,
                    bottom = 4.dp,
                ),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(
                modifier = Modifier
                    .weight(0.54f)
                    .widthIn(max = 420.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                movieDetails.ribbonLabel?.let { ribbon ->
                    RibbonLabelBadge(
                        label = ribbon,
                        size = RibbonLabelBadgeSize.Sm,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                val dynamicTitleSize = when {
                    movieDetails.name.length <= 10 -> 62.sp
                    movieDetails.name.length <= 18 -> 52.sp
                    movieDetails.name.length <= 28 -> 42.sp
                    else -> 34.sp
                }
                val dynamicTitleLine = when {
                    movieDetails.name.length <= 10 -> 58.sp
                    movieDetails.name.length <= 18 -> 48.sp
                    movieDetails.name.length <= 28 -> 38.sp
                    else -> 30.sp
                }
                Text(
                    text = movieDetails.name,
                    color = Color.White,
                    style = ShowcaseHeroStyles.Title.copy(
                        fontSize = dynamicTitleSize,
                        lineHeight = dynamicTitleLine,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                shortLine?.let { line ->
                    Text(
                        text = line,
                        color = StirYellow,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        letterSpacing = (-0.35).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                if (averageRating > 0) {
                    MovieDetailStarRating(
                        averageRating = averageRating,
                        ratingCount = ratingCount,
                        modifier = Modifier.padding(top = 6.dp),
                        starSize = 16.dp,
                    )
                }

                if (movieDetails.description.isNotBlank()) {
                    Text(
                        text = movieDetails.description,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        letterSpacing = (-0.35).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .widthIn(max = 380.dp),
                    )
                }

                MovieDetailInfoLines(movieDetails = movieDetails)

                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .focusGroup(),
                ) {
                    if (purchaseLoading) {
                        MovieDetailSkeletonCtaRow(
                            primaryFocusRequester = primaryFocusRequester,
                        )
                    } else if (hasPurchaseCtas) {
                        MovieDetailPurchaseCtaRow(
                            movie = movieDetails,
                            reminderSet = reminderSet,
                            onPrimaryAction = onPrimaryCtaClick,
                            onSecondaryAction = onSecondaryCtaClick,
                            primaryFocusRequester = primaryFocusRequester,
                        )
                    }

                    MovieDetailSecondaryActions(
                        showTrailer = movieDetails.hasTrailer,
                        showSubtitles = movieDetails.languageRows.isNotEmpty(),
                        onTrailerClick = onTrailerClick,
                        onSubtitlesClick = onOpenLanguages,
                        onShareClick = onShareClick,
                        isBookmarked = isBookmarked,
                        onBookmarkClick = onBookmarkClick,
                        modifier = Modifier.padding(
                            top = if (purchaseLoading || hasPurchaseCtas) 8.dp else 0.dp
                        ),
                        firstItemFocusRequester = if (!purchaseLoading && !hasPurchaseCtas) primaryFocusRequester else null,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.46f)
                    .padding(start = 12.dp, bottom = 4.dp)
                    .widthIn(max = 340.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start,
            ) {
                if (movieDetails.awards.isNotEmpty()) {
                    ShowcaseDetailAwardsRail(
                        awards = movieDetails.awards,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        overlay()
    }
}

fun detailShowcaseInfoLine(movieDetails: MovieDetails): String {
    return listOfNotNull(
        movieDetails.categories.firstOrNull()?.takeIf { it.isNotBlank() },
        movieDetails.releaseYear.takeIf { it.isNotBlank() },
        movieDetails.duration.takeIf { it.isNotBlank() && it != "—" },
    ).joinToString("  •  ")
}
