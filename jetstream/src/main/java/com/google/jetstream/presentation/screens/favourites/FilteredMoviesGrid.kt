package com.google.jetstream.presentation.screens.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.theme.JetStreamBottomListPadding

@Composable
fun FilteredMoviesGrid(
    state: LazyGridState,
    movieList: MovieList,
    onMovieClick: (movieId: String) -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = JetStreamBottomListPadding),
    ) {
        items(movieList, key = { it.id }) { movie ->
            BrewLandscapeMovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                cardWidth = null,
                showTitle = true,
            )
        }
    }
}
