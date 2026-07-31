package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.MovieDetails

private val MetaColor = Color.White.copy(alpha = 0.6f)

/** Single-line hero metadata — year, duration, and genres combined. */
@Composable
fun MovieDetailInfoLines(
    movieDetails: MovieDetails,
    modifier: Modifier = Modifier,
    heroCompact: Boolean = false,
) {
    @Suppress("UNUSED_PARAMETER")
    val unusedCompact = heroCompact

    val combined = buildList {
        movieDetails.releaseYear.takeIf { it.isNotBlank() }?.let { add(it) }
        movieDetails.duration.takeIf { it.isNotBlank() && it != "—" }?.let { add(it) }
        val genres = movieDetails.categories.filter { it.isNotBlank() }
        if (genres.isNotEmpty()) {
            add(genres.joinToString(" • "))
        }
    }.joinToString("       •       ")

    if (combined.isBlank()) return

    Text(
        text = combined,
        color = MetaColor,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = MovieDetailTokens.SynopsisSize,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.4).sp,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(top = 6.dp),
    )
}
