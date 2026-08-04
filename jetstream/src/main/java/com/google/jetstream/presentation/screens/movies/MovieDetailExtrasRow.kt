package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieExtraItem
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.common.BrewMovieCardStyle
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding

private val ExtraCardGap = 18.dp

/** Horizontal rail for bonus clips / episodes — same card styling as related movies. */
@Composable
fun MovieDetailExtrasRow(
    title: String,
    items: List<MovieExtraItem>,
    onItemClick: (MovieExtraItem) -> Unit,
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = 8.dp,
) {
    if (items.isEmpty()) return
    val childPadding = rememberChildPadding()

    Column(modifier = modifier.padding(top = contentTopPadding)) {
        MovieDetailSectionTitle(text = title)
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(ExtraCardGap),
        ) {
            items(items, key = { it.id }) { item ->
                val movie = remember(item) { item.toTrayMovie() }
                val onClick = remember(item, onItemClick) { { onItemClick(item) } }
                BrewLandscapeMovieCard(
                    movie = movie,
                    onClick = onClick,
                    style = BrewMovieCardStyle.Tray,
                )
            }
        }
    }
}

private fun MovieExtraItem.toTrayMovie(): Movie = Movie(
    id = id,
    videoUri = "",
    subtitleUri = null,
    posterUri = thumbnailUri,
    name = title,
    description = subtitle.orEmpty(),
    vodAssetId = vodAssetId,
)
