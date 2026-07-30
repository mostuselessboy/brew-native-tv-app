package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.launch

/**
 * Details hero aligned with vod-frontend `MovieInfoSection`:
 * large Suisse title, synopsis, meta line, single Watch trailer CTA.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieDetails(
    movieDetails: MovieDetails,
    goToMoviePlayer: () -> Unit
) {
    val childPadding = rememberChildPadding()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        MovieBackdrop(
            movieDetails = movieDetails,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.52f)
                .padding(start = childPadding.start, end = 24.dp, bottom = 36.dp),
        ) {
            Text(
                text = movieDetails.name,
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 40.sp,
                letterSpacing = (-1.8).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (movieDetails.description.isNotBlank()) {
                Text(
                    text = movieDetails.description,
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            val meta = listOfNotNull(
                movieDetails.releaseDate.takeIf { it.isNotBlank() },
                movieDetails.categories.take(3).joinToString(", ").takeIf { it.isNotBlank() },
                movieDetails.duration.takeIf { it.isNotBlank() && it != "—" },
            ).joinToString("  •  ")

            if (meta.isNotBlank()) {
                Text(
                    text = meta.uppercase(),
                    color = Color.White.copy(alpha = 0.72f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            if (movieDetails.director != "—") {
                Text(
                    text = "Directed by ${movieDetails.director}",
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            if (movieDetails.videoUri.isNotBlank()) {
                Button(
                    onClick = goToMoviePlayer,
                    modifier = Modifier
                        .padding(top = 22.dp)
                        .widthIn(min = 168.dp)
                        .onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch {
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.colors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        focusedContainerColor = Color.White,
                        focusedContentColor = Color.Black,
                    ),
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.watch_trailer),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = (-0.3).sp,
                    )
                }
            }
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
                                    0.15f to Color.Transparent,
                                    0.55f to scrim.copy(alpha = 0.35f),
                                    0.82f to scrim.copy(alpha = 0.82f),
                                    1f to scrim,
                                )
                            )
                        )
                        drawRect(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0f to scrim.copy(alpha = 0.78f),
                                    0.42f to scrim.copy(alpha = 0.28f),
                                    0.7f to Color.Transparent,
                                )
                            )
                        )
                    },
            )
        }
    }
}
