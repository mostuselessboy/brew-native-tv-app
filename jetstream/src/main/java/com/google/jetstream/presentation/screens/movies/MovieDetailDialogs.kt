package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import kotlinx.coroutines.delay
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.entities.MovieLanguageRow
import com.google.jetstream.data.entities.MovieReviewsAndRatings
import com.google.jetstream.presentation.screens.profile.AccountsSectionDialogButton
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.tvmaterial.StandardDialog
import com.google.jetstream.tvmaterial.Dialog
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

private val DialogSurface = Color(0xFF141414)
private val DialogMuted = Color.White.copy(alpha = 0.45f)
private val MovieDetailDialogShape = RoundedCornerShape(20.dp)

/** Port of mobile-viewer `LanguagesDialog.tsx` — TV StandardDialog. */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTvMaterial3Api::class,
)
@Composable
fun MovieDetailLanguagesDialog(
    showDialog: Boolean,
    rows: List<MovieLanguageRow>,
    onDismissRequest: () -> Unit,
) {
    StandardDialog(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest,
        shape = MovieDetailDialogShape,
        containerColor = DialogSurface,
        title = {
            Text(
                text = "Audio & Subtitles",
                color = Color.White.copy(alpha = 0.92f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = (-0.6).sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Languages",
                        color = DialogMuted,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Audio",
                        color = DialogMuted,
                        fontFamily = BrewTitle,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(58.dp),
                    )
                    Text(
                        text = "Subtitles",
                        color = DialogMuted,
                        fontFamily = BrewTitle,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(58.dp),
                    )
                }

                if (rows.isEmpty()) {
                    Text(
                        text = "No language information available.",
                        color = DialogMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    rows.forEach { row ->
                        LanguageTableRow(row = row)
                    }
                }
            }
        },
        confirmButton = {
            AccountsSectionDialogButton(
                text = "Close",
                shouldRequestFocus = true,
                onClick = onDismissRequest,
                modifier = Modifier.padding(start = 8.dp),
            )
        },
        dismissButton = {},
    )
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTvMaterial3Api::class,
)
@Composable
fun UserReviewDetailDialog(
    review: MovieReviewsAndRatings,
    onDismissRequest: () -> Unit,
) {
    val bodyText = review.reviewBody.trim()
    val title = review.reviewHeading.takeIf { it.isNotBlank() } ?: "Review"
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        runCatching { focusRequester.requestFocus() }
    }

    Dialog(
        showDialog = true,
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .focusRequester(focusRequester)
                .focusable()
                .handleDPadKeyEvents(onEnter = onDismissRequest),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(640.dp)
                    .wrapContentHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF121212),
                                Color(0xFF0A0A0A),
                            ),
                        ),
                        MovieDetailDialogShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), MovieDetailDialogShape)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Column: Avatar, Name & Username
                    Column(
                        modifier = Modifier.width(120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (review.reviewerIconUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(review.reviewerIconUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = review.reviewerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1E1E))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = review.reviewerName.take(1).uppercase(),
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = review.reviewerName,
                            color = Color.White,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        if (review.reviewerUsername.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@${review.reviewerUsername}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Right Column: Stars, Headline, Review text
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Rating stars at the top
                        review.reviewRating?.let { rating ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..5) {
                                    val isFilled = i <= rating
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (isFilled) Color(0xFFFFC15E) else Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$rating/5",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Headline (White, bigger, Swisse font, bold)
                        Text(
                            text = title,
                            color = Color.White,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                        ) {
                            Text(
                                text = bodyText,
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Press OK to close",
                    color = Color.White.copy(alpha = 0.45f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = (-0.2).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTvMaterial3Api::class,
)
@Composable
fun MovieDetailCastDialog(
    castMember: MovieCast,
    castDetails: com.google.jetstream.data.remote.BrewCastMemberDetailDto?,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
    currentMovieName: String? = null,
    currentMovieReleaseDateOrYear: String? = null,
) {
    val context = LocalContext.current
    val bioAvailable = !castDetails?.bio.isNullOrBlank()
    val bio = if (bioAvailable) castDetails!!.bio!!.trim() else "Biography details not available."
    val name = castDetails?.fullName ?: castMember.realName
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        runCatching { focusRequester.requestFocus() }
    }

    val dobFormatted = remember(castDetails?.dob) {
        formatDob(castDetails?.dob)
    }

    val ageText = remember(castDetails?.dob, currentMovieReleaseDateOrYear, currentMovieName) {
        val dobStr = castDetails?.dob ?: return@remember null
        val movieDateStr = currentMovieReleaseDateOrYear ?: return@remember null
        val movieName = currentMovieName ?: return@remember null
        
        val birthYear = dobStr.substringBefore("-").toIntOrNull()
        val filmYear = movieDateStr.substringBefore("-").take(4).toIntOrNull()
        
        if (birthYear != null && filmYear != null) {
            val age = filmYear - birthYear
            if (age in 1..120) {
                age.toString()
            } else null
        } else null
    }

    Dialog(
        showDialog = true,
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .focusRequester(focusRequester)
                .focusable()
                .handleDPadKeyEvents(onEnter = onDismissRequest),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(640.dp)
                    .wrapContentHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF121212),
                                Color(0xFF0A0A0A),
                            ),
                        ),
                        MovieDetailDialogShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), MovieDetailDialogShape)
                    .padding(24.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        com.google.jetstream.presentation.common.Loading()
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Column: Profile Picture
                        Column(
                            modifier = Modifier.width(100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val imgUrl = castDetails?.profileImageUrl ?: castDetails?.imageUrl ?: castDetails?.avatarUrl ?: castMember.avatarUrl
                            if (imgUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imgUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp, 133.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp, 133.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E1E1E))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1).uppercase(),
                                        color = Color.White.copy(alpha = 0.35f),
                                        fontFamily = BrewTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }

                        // Right Column: Details & Bio
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            val specialtyText = castDetails?.knownFor?.takeIf { it.isNotBlank() && it != "N/A" }
                            if (specialtyText != null) {
                                Text(
                                    text = specialtyText.uppercase(),
                                    color = Color(0xFFFFC15E),
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            Text(
                                text = name,
                                color = Color.White,
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val originText = castDetails?.placeOfBirth?.takeIf { it.isNotBlank() && it != "N/A" }
                            val dobText = dobFormatted.takeIf { it.isNotBlank() && it != "N/A" }
                            val genderText = castDetails?.gender?.takeIf { it.isNotBlank() && it != "N/A" }

                            if (originText != null) {
                                Text(
                                    text = originText,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = BrewTitle,
                                    fontSize = 12.sp,
                                )
                            }

                            if (dobText != null) {
                                Text(
                                    text = "Born: $dobText",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = BrewTitle,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }


                            if (ageText != null && currentMovieName != null) {
                                val annotatedAge = androidx.compose.ui.text.buildAnnotatedString {
                                    append("Age while filming ")
                                    withStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFFFC15E))) {
                                        append(currentMovieName)
                                    }
                                    append(": $ageText")
                                }
                                Text(
                                    text = annotatedAge,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = BrewTitle,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp)
                            ) {
                                Text(
                                    text = bio,
                                    color = Color.White.copy(alpha = if (bioAvailable) 0.8f else 0.35f),
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                )
                            }
                        }
                    }

                    // Filmography (Movies he did, posters only, animated marquee loop if count > 5)
                    val moviesList = remember(castDetails) {
                        (castDetails?.topWork.orEmpty() + castDetails?.upcomingWork.orEmpty())
                            .distinctBy { it.id ?: it.slug ?: it.title }
                            .filter {
                                val posterUrl = it.projectPoster ?: it.poster ?: it.posterUri ?: it.posterPath ?: it.thumbnail ?: it.backdrop ?: ""
                                posterUrl.isNotBlank()
                            }
                    }

                    if (moviesList.isNotEmpty()) {
                        val useMarquee = moviesList.size > 5

                        if (useMarquee) {
                            val infiniteListSize = Int.MAX_VALUE
                            val initialIndex = infiniteListSize / 2 - (infiniteListSize / 2 % moviesList.size)
                            val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

                            LaunchedEffect(listState) {
                                while (true) {
                                    try {
                                        listState.scrollBy(0.8f)
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                    delay(16)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "FILMOGRAPHY",
                                color = Color.White.copy(alpha = 0.4f),
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyRow(
                                state = listState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().height(95.dp)
                            ) {
                                items(infiniteListSize) { index ->
                                    val movie = moviesList[index % moviesList.size]
                                    val posterUrl = movie.projectPoster ?: movie.poster ?: movie.posterUri ?: movie.posterPath ?: movie.thumbnail ?: movie.backdrop ?: ""
                                    
                                    if (posterUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(com.google.jetstream.data.util.BrewImageUrl.forCast(posterUrl))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = movie.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 60.dp, height = 90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        } else {
                            // Static layout for small list
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "FILMOGRAPHY",
                                color = Color.White.copy(alpha = 0.4f),
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().height(95.dp)
                            ) {
                                items(moviesList) { movie ->
                                    val posterUrl = movie.projectPoster ?: movie.poster ?: movie.posterUri ?: movie.posterPath ?: movie.thumbnail ?: movie.backdrop ?: ""
                                    
                                    if (posterUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(com.google.jetstream.data.util.BrewImageUrl.forCast(posterUrl))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = movie.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 60.dp, height = 90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Press OK to close",
                    color = Color.White.copy(alpha = 0.45f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = (-0.2).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatDob(dobStr: String?): String {
    if (dobStr.isNullOrBlank()) return "N/A"
    return try {
        val clean = dobStr.substringBefore("T")
        val parts = clean.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = when (parts[1].toIntOrNull()) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> parts[1]
            }
            val day = parts[2].toIntOrNull()?.toString() ?: parts[2]
            "$month $day, $year"
        } else {
            dobStr
        }
    } catch (e: Exception) {
        dobStr
    }
}

@Composable
private fun LanguageTableRow(row: MovieLanguageRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.displayName,
            color = Color.White.copy(alpha = 0.88f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        AvailabilityCell(available = row.hasAudio, modifier = Modifier.width(58.dp))
        AvailabilityCell(available = row.hasSubtitles, modifier = Modifier.width(58.dp))
    }
}

@Composable
private fun AvailabilityCell(available: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (available) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (available) MovieDetailTokens.CheckGreen else MovieDetailTokens.XGray,
            modifier = Modifier.size(16.dp),
        )
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTvMaterial3Api::class,
)
@Composable
fun MovieDetailSynopsisDialog(
    showDialog: Boolean,
    title: String,
    synopsis: String,
    onDismissRequest: () -> Unit,
) {
    StandardDialog(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest,
        shape = MovieDetailDialogShape,
        containerColor = DialogSurface,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        },
        text = {
            Text(
                text = synopsis,
                color = Color.White.copy(alpha = 0.86f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            AccountsSectionDialogButton(
                text = "Close",
                shouldRequestFocus = true,
                onClick = onDismissRequest,
                modifier = Modifier.padding(start = 8.dp),
            )
        },
        dismissButton = {},
    )
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTvMaterial3Api::class,
)
@Composable
fun MovieDetailAccessDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    showSignInButton: Boolean,
    showBuyButton: Boolean,
    onSignInClick: () -> Unit,
    onBuyClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    StandardDialog(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest,
        shape = MovieDetailDialogShape,
        containerColor = DialogSurface,
        title = {
            Text(
                text = title,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = BrewTitle,
                fontSize = 13.sp,
                color = DialogMuted,
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(showDialog) {
                    if (showDialog) {
                        focusRequester.requestFocus()
                    }
                }
                if (showSignInButton) {
                    AccountsSectionDialogButton(
                        text = "Sign In",
                        shouldRequestFocus = false,
                        onClick = {
                            onDismissRequest()
                            onSignInClick()
                        },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }
                if (showBuyButton) {
                    AccountsSectionDialogButton(
                        text = "View Purchase Options",
                        shouldRequestFocus = false,
                        onClick = {
                            onDismissRequest()
                            onBuyClick()
                        },
                        modifier = if (!showSignInButton) Modifier.focusRequester(focusRequester) else Modifier
                    )
                }
                AccountsSectionDialogButton(
                    text = "Cancel",
                    shouldRequestFocus = !showSignInButton && !showBuyButton,
                    onClick = onDismissRequest,
                    modifier = if (!showSignInButton && !showBuyButton) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        },
        dismissButton = {}
    )
}
