/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.search

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private val SearchBarShape = RoundedCornerShape(14.dp)
private val SearchBarBg = Color(0x14FFFFFF)
private val SearchBarFocusedBorder = Color.White.copy(alpha = 0.42f)
private val SearchBarIdleBorder = Color.White.copy(alpha = 0.10f)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    onMovieClick: (movie: Movie) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    sidebarFocusRequester: FocusRequester? = null,
    contentFocusRequester: FocusRequester? = null,
    isTabVisible: Boolean = true,
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel(),
) {
    val lazyColumnState = rememberLazyListState()
    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyColumnState.firstVisibleItemIndex == 0 &&
                lazyColumnState.firstVisibleItemScrollOffset < 100
        }
    }

    val searchState by searchScreenViewModel.searchState.collectAsStateWithLifecycle()

    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF0A0A0A),
                        0.35f to Color(0xFF050505),
                        1f to Color.Black,
                    ),
                ),
            ),
    ) {
        when (val s = searchState) {
            SearchState.Loading,
            SearchState.Searching -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (s == SearchState.Loading) "Loading…" else "Searching…",
                        color = Color.White.copy(alpha = 0.55f),
                        fontFamily = BrewTitle,
                        fontSize = 14.sp,
                    )
                }
            }

            is SearchState.Done -> {
                SearchResult(
                    movieList = s.movieList,
                    sectionTitle = s.sectionTitle,
                    sectionSubheading = s.sectionSubheading,
                    searchQuery = s.searchQuery,
                    isSuggestions = s.isSuggestions,
                    searchMovies = searchScreenViewModel::query,
                    onMovieClick = onMovieClick,
                    sidebarFocusRequester = sidebarFocusRequester,
                    contentFocusRequester = contentFocusRequester,
                    isTabVisible = isTabVisible,
                    lazyColumnState = lazyColumnState,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchResult(
    movieList: MovieList,
    sectionTitle: String?,
    sectionSubheading: String?,
    searchQuery: String?,
    isSuggestions: Boolean,
    searchMovies: (queryString: String) -> Unit,
    onMovieClick: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier,
    lazyColumnState: LazyListState = rememberLazyListState(),
    sidebarFocusRequester: FocusRequester? = null,
    contentFocusRequester: FocusRequester? = null,
    isTabVisible: Boolean = true,
) {
    val childPadding = rememberChildPadding()
    var searchQueryText by remember { mutableStateOf("") }
    val localFocusRequester = remember { FocusRequester() }
    val tfFocusRequester = contentFocusRequester ?: localFocusRequester
    val focusManager = LocalFocusManager.current
    val tfInteractionSource = remember { MutableInteractionSource() }

    val isTfFocused by tfInteractionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isTfFocused) SearchBarFocusedBorder else SearchBarIdleBorder,
        label = "searchBarBorder",
    )

    LaunchedEffect(isTabVisible) {
        if (isTabVisible) {
            tfFocusRequester.requestFocus()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyColumnState,
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = childPadding.start,
                        end = childPadding.end,
                        top = 20.dp,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.search_screen_heading),
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = (-1.2).sp,
                )
                Text(
                    text = stringResource(R.string.search_screen_subheading),
                    color = Color.White.copy(alpha = 0.55f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(50.dp)
                        .clip(SearchBarShape)
                        .background(SearchBarBg)
                        .border(1.dp, borderColor, SearchBarShape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_search),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = if (isTfFocused) 0.85f else 0.45f),
                        modifier = Modifier.size(20.dp),
                    )
                    BasicTextField(
                        value = searchQueryText,
                        onValueChange = { updatedQuery -> searchQueryText = updatedQuery },
                        decorationBox = { innerTextField ->
                            Box {
                                innerTextField()
                                if (searchQueryText.isEmpty()) {
                                    Text(
                                        modifier = Modifier.graphicsLayer { alpha = 0.45f },
                                        text = stringResource(R.string.search_screen_et_placeholder),
                                        style = searchFieldStyle(),
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(tfFocusRequester)
                            .then(
                                if (sidebarFocusRequester != null) {
                                    Modifier.focusProperties { left = sidebarFocusRequester }
                                } else {
                                    Modifier
                                },
                            )
                            .onKeyEvent {
                                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                                    when (it.nativeKeyEvent.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            focusManager.moveFocus(FocusDirection.Down)
                                        }
                                        KeyEvent.KEYCODE_DPAD_UP -> {
                                            focusManager.moveFocus(FocusDirection.Up)
                                        }
                                        KeyEvent.KEYCODE_BACK -> {
                                            focusManager.moveFocus(FocusDirection.Exit)
                                        }
                                    }
                                }
                                true
                            },
                        cursorBrush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color.White),
                        ),
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search,
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { searchMovies(searchQueryText) },
                        ),
                        maxLines = 1,
                        interactionSource = tfInteractionSource,
                        textStyle = searchFieldStyle(),
                    )
                }
            }
        }

        item {
            val rowTitle = when {
                !isSuggestions && searchQuery != null ->
                    "Results for \"$searchQuery\""
                sectionTitle != null -> sectionTitle
                else -> null
            }
            val rowSubtitle = if (isSuggestions) sectionSubheading else null

            if (movieList.isEmpty()) {
                Text(
                    text = if (searchQuery != null) {
                        "No titles match \"$searchQuery\""
                    } else {
                        "No suggestions right now"
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = BrewTitle,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(
                            start = childPadding.start,
                            top = childPadding.top * 2,
                        ),
                )
            } else {
                MoviesRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = childPadding.top * 1.5f),
                    movieList = movieList,
                    title = rowTitle,
                    subtitle = rowSubtitle,
                    titleStyle = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        letterSpacing = (-0.5).sp,
                    ),
                    onMovieSelected = { selectedMovie -> onMovieClick(selectedMovie) },
                    leftFocusRequester = sidebarFocusRequester,
                )
            }
        }
    }
}

private fun searchFieldStyle(): TextStyle = TextStyle(
    color = Color.White,
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    letterSpacing = (-0.2).sp,
)
