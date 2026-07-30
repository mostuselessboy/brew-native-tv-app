package com.google.jetstream.presentation.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.presentation.common.HomeShimmerSkeleton
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

@Composable
fun MyLibraryScreen(
    onMovieClick: (String) -> Unit,
    openAuth: () -> Unit,
    onBrowseHome: () -> Unit = {},
    viewModel: MyLibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    when (val state = uiState) {
        MyLibraryUiState.Loading -> HomeShimmerSkeleton(modifier = Modifier.fillMaxSize())
        MyLibraryUiState.SignInRequired -> LibrarySignInPrompt(onSignIn = openAuth)
        is MyLibraryUiState.Error -> LibraryMessage(
            title = "Couldn't load your library",
            subtitle = state.message,
        )
        is MyLibraryUiState.Ready -> {
            if (state.content.isEmpty) {
                LibraryEmpty(onBrowse = onBrowseHome)
            } else {
                LibraryContent(
                    content = state.content,
                    onMovieClick = onMovieClick,
                )
            }
        }
    }
}

@Composable
private fun LibraryContent(
    content: com.google.jetstream.data.repositories.MyLibraryContent,
    onMovieClick: (String) -> Unit,
) {
    val padding = rememberChildPadding()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = padding.start,
            end = padding.end,
            top = 28.dp,
            bottom = 72.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusGroup(),
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "My Library",
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    letterSpacing = (-1.2).sp,
                    lineHeight = 42.sp,
                )
                Text(
                    text = "Your rented and purchased movies",
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                    ),
                )
            }
        }

        if (content.rented.isNotEmpty()) {
            item(key = "rented") {
                LibraryTray(
                    title = "Rented",
                    subheading = "Your active rentals",
                    movies = content.rented,
                    onMovieClick = onMovieClick,
                )
            }
        }
        if (content.purchased.isNotEmpty()) {
            item(key = "purchased") {
                LibraryTray(
                    title = "Purchased",
                    subheading = "Yours to keep",
                    movies = content.purchased,
                    onMovieClick = onMovieClick,
                )
            }
        }
        if (content.expired.isNotEmpty()) {
            item(key = "expired") {
                LibraryTray(
                    title = "Rent Again",
                    subheading = "Your expired rentals — ready to watch again",
                    movies = content.expired,
                    onMovieClick = onMovieClick,
                )
            }
        }
    }
}

@Composable
private fun LibraryTray(
    title: String,
    subheading: String,
    movies: List<Movie>,
    onMovieClick: (String) -> Unit,
) {
    MoviesRow(
        title = title,
        subheading = subheading,
        movieList = movies,
        onMovieSelected = { onMovieClick(it.id) },
        titleStyle = MaterialTheme.typography.titleMedium.copy(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
        ),
    )
}

@Composable
private fun LibrarySignInPrompt(onSignIn: () -> Unit) {
    LibraryMessage(
        title = "Sign in to view your library",
        subtitle = "Scan the QR code on your phone to access rentals and purchases.",
        actionLabel = "Sign in",
        onAction = onSignIn,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryEmpty(onBrowse: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 48.dp),
        ) {
            Text(
                text = "Your library is empty",
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            )
            Text(
                text = "Rent or buy movies to see them here",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 18.sp,
            )
            Button(onClick = onBrowse) {
                Text(
                    text = "BROWSE TITLES",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryMessage(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(32.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 32.sp,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                lineHeight = 22.sp,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
