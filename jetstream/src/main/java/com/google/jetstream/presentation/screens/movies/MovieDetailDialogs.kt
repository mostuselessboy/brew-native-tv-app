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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.MovieLanguageRow
import com.google.jetstream.data.entities.MovieReviewsAndRatings
import com.google.jetstream.presentation.screens.profile.AccountsSectionDialogButton
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.tvmaterial.StandardDialog

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

    StandardDialog(
        showDialog = true,
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
            if (bodyText.isNotBlank()) {
                Text(
                    text = bodyText,
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
