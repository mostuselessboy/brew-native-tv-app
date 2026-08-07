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
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.common.BrewVerticalMovieCard
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private val SearchBarShape = RoundedCornerShape(14.dp)
private val SearchBarBg = Color(0x14FFFFFF)
private val SearchBarFocusedBorder = Color.White.copy(alpha = 0.42f)
private val SearchBarIdleBorder = Color.White.copy(alpha = 0.10f)

private const val GRID_COLUMNS = 5
private const val RESULT_CAP = 48

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
    val lazyGridState = rememberLazyGridState()
    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex == 0 &&
                lazyGridState.firstVisibleItemScrollOffset < 100
        }
    }

    val searchState by searchScreenViewModel.searchState.collectAsStateWithLifecycle()
    val selectedFilter by searchScreenViewModel.selectedFilter.collectAsStateWithLifecycle()
    var lastDone by remember { mutableStateOf<SearchState.Done?>(null) }
    LaunchedEffect(searchState) {
        if (searchState is SearchState.Done) {
            lastDone = searchState as SearchState.Done
        }
    }

    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusProperties {
                up = com.google.jetstream.presentation.screens.dashboard.SearchTopBarFocusRequester
            }
            .focusGroup(),
    ) {
        when (searchState) {
            SearchState.Loading -> {
                SearchLoadingPlaceholder(
                    message = "Loading\u2026",
                    contentFocusRequester = contentFocusRequester,
                )
            }

            SearchState.Searching,
            is SearchState.Done,
            -> {
                val done = searchState as? SearchState.Done ?: lastDone
                SearchResult(
                    movieList = done?.movieList ?: emptyList(),
                    sectionTitle = done?.sectionTitle,
                    sectionSubheading = done?.sectionSubheading,
                    searchQuery = done?.searchQuery,
                    isSuggestions = done?.isSuggestions == true,
                    isSearching = searchState == SearchState.Searching,
                    searchMovies = searchScreenViewModel::query,
                    onMovieClick = onMovieClick,
                    sidebarFocusRequester = sidebarFocusRequester,
                    contentFocusRequester = contentFocusRequester,
                    isTabVisible = isTabVisible,
                    lazyGridState = lazyGridState,
                    selectedFilter = selectedFilter,
                    onFilterSelected = searchScreenViewModel::setFilter,
                )
            }
        }
    }
}

@Composable
private fun SearchLoadingPlaceholder(
    message: String,
    contentFocusRequester: FocusRequester?,
) {
    val localFocusRequester = remember { FocusRequester() }
    val focusRequester = contentFocusRequester ?: localFocusRequester

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (contentFocusRequester != null) {
                    Modifier
                        .focusRequester(focusRequester)
                        .focusable()
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        com.google.jetstream.presentation.common.BrewSpinner(size = 42.dp)
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
    isSearching: Boolean = false,
    searchMovies: (queryString: String) -> Unit,
    onMovieClick: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    sidebarFocusRequester: FocusRequester? = null,
    contentFocusRequester: FocusRequester? = null,
    isTabVisible: Boolean = true,
    selectedFilter: SearchFilter = SearchFilter.All,
    onFilterSelected: (SearchFilter) -> Unit = {},
) {
    val childPadding = rememberChildPadding()
    var searchQueryText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val localBarFocusRequester = remember { FocusRequester() }
    val barFocusRequester = contentFocusRequester ?: localBarFocusRequester
    val textFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val tfInteractionSource = remember { MutableInteractionSource() }
    val barInteractionSource = remember { MutableInteractionSource() }

    val isBarFocused by barInteractionSource.collectIsFocusedAsState()
    val isTfFocused by tfInteractionSource.collectIsFocusedAsState()
    val isBarHighlighted = isBarFocused || isTfFocused
    val borderColor by animateColorAsState(
        targetValue = if (isBarHighlighted) SearchBarFocusedBorder else SearchBarIdleBorder,
        label = "searchBarBorder",
    )

    // Instant client-side filter: derived synchronously, no network call, no StateFlow emission.
    val filteredMovies = remember(movieList, selectedFilter) {
        movieList.applySearchFilter(selectedFilter).take(RESULT_CAP)
    }

    val rowTitle = remember(isSuggestions, searchQuery, sectionTitle) {
        when {
            !isSuggestions && searchQuery != null -> "Results for \"$searchQuery\""
            sectionTitle != null -> sectionTitle
            else -> null
        }
    }

    LaunchedEffect(isTabVisible) {
        if (isTabVisible) isSearchActive = false
    }
    LaunchedEffect(isTfFocused) {
        if (!isTfFocused) isSearchActive = false
    }

    // LazyVerticalGrid replaces LazyColumn + manual Row/Column grid.
    // Only visible card rows are composed — fixes initial-load lag and filter-change recomposition.
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        state = lazyGridState,
        contentPadding = PaddingValues(
            start = childPadding.start,
            end = childPadding.end,
            top = childPadding.top + 80.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize(),
    ) {

        // ── Header: title + search bar ────────────────────────────────────────────
        item(key = "header", contentType = "header", span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 4.dp),
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

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(50.dp)
                        .clip(SearchBarShape)
                        .background(SearchBarBg)
                        .border(1.dp, borderColor, SearchBarShape)
                        .focusRequester(barFocusRequester)
                        .focusable(interactionSource = barInteractionSource)
                        .focusProperties {
                            up = com.google.jetstream.presentation.screens.dashboard.SearchTopBarFocusRequester
                        }
                        .onKeyEvent { event ->
                            if (
                                !isSearchActive &&
                                event.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                                (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                            ) {
                                isSearchActive = true
                                runCatching { textFocusRequester.requestFocus() }
                                true
                            } else false
                        }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_search),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = if (isBarHighlighted) 0.85f else 0.45f),
                        modifier = Modifier.size(20.dp),
                    )
                    BasicTextField(
                        value = searchQueryText,
                        onValueChange = { updatedQuery ->
                            searchQueryText = updatedQuery
                            searchMovies(updatedQuery)
                        },
                        readOnly = !isSearchActive,
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
                            .focusRequester(textFocusRequester)
                            .focusProperties {
                                canFocus = isSearchActive
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                            }
                            .onKeyEvent { event ->
                                if (event.nativeKeyEvent.action != KeyEvent.ACTION_UP) {
                                    return@onKeyEvent false
                                }
                                when (event.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        focusManager.moveFocus(FocusDirection.Down)
                                        true
                                    }
                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        focusManager.moveFocus(FocusDirection.Up)
                                        true
                                    }
                                    KeyEvent.KEYCODE_BACK -> {
                                        if (isSearchActive) {
                                            isSearchActive = false
                                            runCatching { barFocusRequester.requestFocus() }
                                        } else {
                                            focusManager.moveFocus(FocusDirection.Exit)
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            },
                        cursorBrush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color.White),
                        ),
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search,
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                searchMovies(searchQueryText)
                                isSearchActive = false
                                runCatching { barFocusRequester.requestFocus() }
                            },
                        ),
                        maxLines = 1,
                        interactionSource = tfInteractionSource,
                        textStyle = searchFieldStyle(),
                    )
                }
                if (!isSearchActive) {
                    Text(
                        text = stringResource(R.string.search_screen_bar_action),
                        color = Color.White.copy(alpha = if (isBarFocused) 0.62f else 0.42f),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = (-0.1).sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        // ── Filter pill row ───────────────────────────────────────────────────────
        item(key = "filter_pills", contentType = "filter", span = { GridItemSpan(maxLineSpan) }) {
            SearchFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
                barFocusRequester = barFocusRequester,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
            )
        }

        // ── Searching indicator ───────────────────────────────────────────────────
        if (isSearching) {
            item(key = "searching", contentType = "status", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Searching\u2026",
                    color = Color.White.copy(alpha = 0.45f),
                    fontFamily = BrewTitle,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
        }

        // ── Section title ─────────────────────────────────────────────────────────
        if (filteredMovies.isNotEmpty()) {
            rowTitle?.let { title ->
                item(key = "section_title", contentType = "title", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                    )
                }
            }
        }

        // ── Empty state ───────────────────────────────────────────────────────────
        if (filteredMovies.isEmpty() && !isSearching) {
            item(key = "empty", contentType = "status", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (searchQuery != null) {
                        "No titles match \"$searchQuery\""
                    } else {
                        "No suggestions right now"
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = BrewTitle,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        // ── Movie cards ───────────────────────────────────────────────────────────
        itemsIndexed(
            items = filteredMovies,
            key = { _, movie -> movie.id },
            contentType = { _, _ -> "card" },
        ) { index, movie ->
            BrewVerticalMovieCard(
                movie = movie,
                onClick = { onMovieClick(movie) },
                fillWidth = true,
                showTitle = false,
                modifier = Modifier.then(
                    if (index == 0 && sidebarFocusRequester != null) {
                        Modifier.focusProperties { left = sidebarFocusRequester }
                    } else Modifier,
                ),
            )
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

private val FilterPillShape = RoundedCornerShape(999.dp)

@Composable
private fun SearchFilterRow(
    selectedFilter: SearchFilter,
    onFilterSelected: (SearchFilter) -> Unit,
    barFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .then(
                if (barFocusRequester != null) {
                    Modifier.focusProperties { up = barFocusRequester }
                } else Modifier
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchFilter.entries.forEach { filter ->
            SearchFilterPill(
                filter = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
}

@Composable
private fun SearchFilterPill(
    filter: SearchFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val pillBg = when {
        isSelected -> Color.White
        isFocused -> Color(0x40FFFFFF)
        else -> Color(0x1AFFFFFF)
    }
    val textColor = if (isSelected) Color.Black else Color.White

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(FilterPillShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = pillBg,
            focusedContainerColor = if (isSelected) Color.White else Color(0x40FFFFFF),
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                shape = FilterPillShape,
            ),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        modifier = Modifier.onFocusChanged { isFocused = it.isFocused },
    ) {
        Text(
            text = filter.label,
            color = textColor,
            fontFamily = BrewTitle,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = (-0.2).sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
