package com.google.jetstream.presentation.screens.movies

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.presentation.common.ShowcaseHeroBackdrop
import com.google.jetstream.presentation.common.ShowcaseHeroStyles
import com.google.jetstream.presentation.common.ShowcaseHeight
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private val StirYellow = MovieDetailTokens.AccentYellow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MovieDetailShowcaseHero(
    movieDetails: MovieDetails,
    onPrimaryCtaClick: () -> Unit,
    onSecondaryCtaClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onOpenLanguages: () -> Unit,
    primaryCtaFocusRequester: FocusRequester? = null,
    secondaryActionsFocusRequester: FocusRequester? = null,
    upFocusRequester: FocusRequester? = null,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val childPadding = rememberChildPadding()
    val secondaryFocusRequester = secondaryActionsFocusRequester ?: remember { FocusRequester() }
    val purchaseSlots = remember(movieDetails) { DetailPurchaseCta.primaryRowSlots(movieDetails) }
    val hasPurchaseCtas = purchaseSlots.isNotEmpty()
    val shortLine = movieDetails.tagline.takeIf { it.isNotBlank() }
    val averageRating = movieDetails.averageRating
        ?: movieDetails.reviewSummary?.averageRating
        ?: 0.0
    val ratingCount = movieDetails.ratingCount
        ?: movieDetails.reviewSummary?.totalRatings
        ?: 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ShowcaseHeight)
            .clipToBounds()
            .background(Color.Black),
    ) {
        ShowcaseHeroBackdrop(
            posterUri = movieDetails.posterUri,
            contentDescription = movieDetails.name,
            modifier = Modifier.fillMaxSize(),
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
                val dynamicTitleSize = when {
                    movieDetails.name.length <= 10 -> 54.sp
                    movieDetails.name.length <= 18 -> 46.sp
                    movieDetails.name.length <= 28 -> 38.sp
                    else -> 30.sp
                }
                Text(
                    text = movieDetails.name,
                    color = Color.White,
                    style = ShowcaseHeroStyles.Title.copy(fontSize = dynamicTitleSize),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                shortLine?.let { line ->
                    Text(
                        text = line,
                        color = StirYellow,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
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
                        starSize = 14.dp,
                    )
                }

                MovieDetailInfoLines(movieDetails = movieDetails)

                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .focusGroup(),
                ) {
                    if (hasPurchaseCtas) {
                        MovieDetailPurchaseCtaRow(
                            movie = movieDetails,
                            onPrimaryAction = onPrimaryCtaClick,
                            onSecondaryAction = onSecondaryCtaClick,
                            primaryFocusRequester = primaryCtaFocusRequester,
                            upFocusRequester = upFocusRequester,
                        )
                    }

                    MovieDetailSecondaryActions(
                        showTrailer = movieDetails.hasTrailer,
                        showSubtitles = movieDetails.languageRows.isNotEmpty(),
                        onTrailerClick = onTrailerClick,
                        onSubtitlesClick = onOpenLanguages,
                        firstFocusRequester = secondaryFocusRequester,
                        isBookmarked = isBookmarked,
                        onBookmarkClick = onBookmarkClick,
                        modifier = Modifier.padding(top = if (hasPurchaseCtas) 8.dp else 0.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.46f)
                    .padding(start = 12.dp, bottom = 2.dp)
                    .widthIn(max = 340.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start,
            ) {
                if (movieDetails.awards.isNotEmpty()) {
                    ShowcaseDetailAwardsRail(
                        awards = movieDetails.awards,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                }

                if (movieDetails.description.isNotBlank()) {
                    Text(
                        text = movieDetails.description,
                        color = Color.White.copy(alpha = 0.82f),
                        style = ShowcaseHeroStyles.Description.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
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
