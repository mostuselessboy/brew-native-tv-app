package com.google.jetstream.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.text.PlatformImeOptions
import androidx.tv.foundation.text.TvKeyboardAlignment
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.common.BrewFocusedCardFrame
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.delay

private val SearchBg = Color.Black
private val SearchFieldBg = Color(0xFF141414)
private val SearchHint = Color(0xFF8A8A8F)

@OptIn(ExperimentalTvFoundationApi::class)
@Composable
fun SearchScreen(
    onMovieClick: (movie: Movie) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel(),
) {
    val uiState by searchScreenViewModel.uiState.collectAsStateWithLifecycle()
    val padding = rememberChildPadding()
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    onScroll(true)

    LaunchedEffect(Unit) {
        delay(120)
        runCatching { searchFocusRequester.requestFocus() }
        keyboardController?.show()
    }

    val showResults = uiState.debouncedQuery.length >= 2
    val gridMovies = if (showResults) uiState.results else uiState.suggestions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SearchBg)
            .imePadding()
            .padding(
                start = padding.start,
                end = padding.end,
                top = padding.top,
                bottom = padding.bottom,
            ),
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = searchScreenViewModel::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) keyboardController?.show()
                },
            placeholder = {
                Text(
                    text = "Search movies and shows",
                    color = SearchHint,
                    fontFamily = BrewTitle,
                )
            },
            singleLine = true,
            textStyle = TvMaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SearchFieldBg,
                unfocusedContainerColor = SearchFieldBg,
                disabledContainerColor = SearchFieldBg,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                platformImeOptions = PlatformImeOptions(TvKeyboardAlignment.Left),
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() },
            ),
        )

        if (!showResults && uiState.suggestions.isNotEmpty()) {
            Text(
                text = "Suggested",
                color = Color.White.copy(alpha = 0.72f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
            )
        }

        TvSearchResultsGrid(
            movies = gridMovies,
            isFetching = uiState.isFetching && showResults,
            emptyMessage = if (showResults && uiState.results.isEmpty()) {
                "No match named \"${uiState.debouncedQuery}\" found."
            } else {
                null
            },
            onMovieClick = onMovieClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = if (showResults) 20.dp else 0.dp),
        )
    }
}

@Composable
private fun TvSearchResultsGrid(
    movies: List<Movie>,
    isFetching: Boolean,
    emptyMessage: String?,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isFetching && movies.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "Searching…", color = SearchHint, fontFamily = BrewTitle)
        }
        return
    }
    if (movies.isEmpty() && emptyMessage != null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = emptyMessage,
                color = SearchHint,
                fontFamily = BrewTitle,
                fontSize = 16.sp,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(movies, key = { it.id }) { movie ->
            TvSearchResultCard(movie = movie, onClick = { onMovieClick(movie) })
        }
    }
}

@Composable
private fun TvSearchResultCard(
    movie: Movie,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(8.dp)

    BrewFocusedCardFrame(
        onClick = onClick,
        shape = shape,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(Color(0xFF16161A), shape),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(BrewImageUrl.forCard(movie.posterUri))
                    .size(BrewImageUrl.CARD_WIDTH, BrewImageUrl.CARD_HEIGHT)
                    .build(),
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(8.dp),
            ) {
                Text(
                    text = movie.name,
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                movie.year?.let { year ->
                    Text(
                        text = year,
                        color = Color.White.copy(alpha = 0.6f),
                        style = TvMaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
