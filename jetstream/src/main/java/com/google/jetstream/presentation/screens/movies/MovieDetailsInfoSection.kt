package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

@Composable
fun MovieDetailsInfoSection(
    movieDetails: MovieDetails,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val rows = buildList {
        if (movieDetails.originalLanguage.isNotBlank() && movieDetails.originalLanguage != "—") {
            add("Language" to movieDetails.originalLanguage)
        }
        if (movieDetails.status.isNotBlank() && movieDetails.status != "—") {
            add("Status" to movieDetails.status)
        }
        if (movieDetails.screenplay.isNotBlank() && movieDetails.screenplay != "—") {
            add("Screenplay" to movieDetails.screenplay)
        }
        if (movieDetails.music.isNotBlank() && movieDetails.music != "—") {
            add("Music" to movieDetails.music)
        }
        if (movieDetails.budget.isNotBlank() && movieDetails.budget != "—") {
            add("Budget" to movieDetails.budget)
        }
    }
    if (rows.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = childPadding.start)
            .padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Details",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = (-0.4).sp,
        )
        rows.forEach { (label, value) ->
            DetailInfoRow(label = label, value = value)
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(0.22f),
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.88f),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.weight(1f),
        )
    }
}
