package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.jetstream.presentation.common.TrayCardPlaceholder
import kotlinx.coroutines.delay
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.common.BrewMovieCardStyle
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding

private val DetailRelatedCardGap = 12.dp

/** Detail-page related / also-watched rail — shared modular card styling. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DetailRelatedMoviesRow(
    title: String,
    movieList: MovieList,
    onMovieSelected: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    firstItemFocusRequester: FocusRequester? = null,
    contentTopPadding: Dp = 24.dp,
    deferCardMount: Boolean = false,
    deferCardMountDelayMs: Long = 80,
) {
    if (movieList.isEmpty()) return
    val childPadding = rememberChildPadding()
    var cardsReady by remember(movieList, deferCardMount) {
        mutableStateOf(!deferCardMount)
    }
    LaunchedEffect(movieList, deferCardMount, deferCardMountDelayMs) {
        if (deferCardMount) {
            delay(deferCardMountDelayMs)
            cardsReady = true
        }
    }

    Column(modifier = modifier.padding(top = contentTopPadding)) {
        MovieDetailSectionTitle(text = title)
        LazyRow(
            modifier = Modifier.padding(top = 14.dp),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(DetailRelatedCardGap),
        ) {
            items(movieList, key = { it.id }) { movie ->
                val cardModifier = Modifier
                    .then(
                        if (firstItemFocusRequester != null && movie.id == movieList.first().id) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        }
                    )
                if (!cardsReady) {
                    TrayCardPlaceholder(modifier = cardModifier)
                } else {
                    BrewLandscapeMovieCard(
                        movie = movie,
                        onClick = { onMovieSelected(movie) },
                        style = BrewMovieCardStyle.Tray,
                        modifier = cardModifier,
                    )
                }
            }
        }
    }
}
