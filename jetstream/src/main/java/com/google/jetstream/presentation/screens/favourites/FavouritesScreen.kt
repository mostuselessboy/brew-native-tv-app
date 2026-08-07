package com.google.jetstream.presentation.screens.favourites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.LibraryCardLayout
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.LibraryShelf
import com.google.jetstream.data.entities.LibraryShelfId
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.data.util.LibraryClickAction
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.common.ItemDirection
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.screens.movies.MovieDetailTokens
import com.google.jetstream.presentation.theme.BrewTitle

private val LibraryBackground = Color.Black
private val LibraryPortraitWidth = 150.dp
private val LibraryPortraitArtHeight = 225.dp
private val LibraryPortraitRadius = 8.dp
private val LibraryViewMoreWidth = 104.dp
private val LibraryShelfSpacing = 28.dp
private val LibraryShelfTitleSize = 14.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FavouritesScreen(
    onMovieClick: (movieId: String) -> Unit,
    onLibraryItemClick: (LibraryItem) -> Unit = { onMovieClick(it.movieId) },
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    onSignInClick: () -> Unit = {},
    onBrowseHome: () -> Unit = {},
    onBrowseStore: () -> Unit = {},
    sidebarFocusRequester: FocusRequester? = null,
    contentFocusRequester: FocusRequester? = null,
    isTabVisible: Boolean = true,
    viewModel: MyLibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localContentFocus = remember { FocusRequester() }
    val firstContentFocus = contentFocusRequester ?: localContentFocus
    onScroll(true)

    LaunchedEffect(isTabVisible) {
        if (isTabVisible) {
            viewModel.refreshIfPending()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LibraryBackground),
    ) {
        if (isTabVisible) {
            when (val state = uiState) {
                MyLibraryUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
                MyLibraryUiState.Guest -> LibraryGuestState(
                    onSignInClick = onSignInClick,
                    focusRequester = firstContentFocus,
                    sidebarFocusRequester = sidebarFocusRequester,
                )
                is MyLibraryUiState.Error -> LibraryErrorState(
                    message = state.message,
                    onRetry = viewModel::refresh,
                    focusRequester = firstContentFocus,
                    sidebarFocusRequester = sidebarFocusRequester,
                )
                is MyLibraryUiState.Empty -> LibraryEmptyState(
                    userName = state.userName,
                    avatarUrl = state.avatarUrl,
                    onBrowse = onBrowseHome,
                    focusRequester = firstContentFocus,
                    sidebarFocusRequester = sidebarFocusRequester,
                )
                is MyLibraryUiState.Ready -> MyLibraryContent(
                    userName = state.userName,
                    avatarUrl = state.avatarUrl,
                    shelves = state.page.shelves,
                    loadingShelfId = state.loadingShelfId,
                    onMovieClick = onMovieClick,
                    onLibraryItemClick = onLibraryItemClick,
                    onLoadMore = viewModel::loadMoreShelf,
                    onBrowseHome = onBrowseHome,
                    onBrowseStore = onBrowseStore,
                    firstContentFocusRequester = firstContentFocus,
                    sidebarFocusRequester = sidebarFocusRequester,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MyLibraryContent(
    userName: String?,
    avatarUrl: String?,
    shelves: List<LibraryShelf>,
    loadingShelfId: LibraryShelfId?,
    onMovieClick: (String) -> Unit,
    onLibraryItemClick: (LibraryItem) -> Unit,
    onLoadMore: (LibraryShelf) -> Unit,
    onBrowseHome: () -> Unit,
    onBrowseStore: () -> Unit,
    firstContentFocusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester?,
) {
    val childPadding = rememberChildPadding()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = childPadding.start,
            end = childPadding.end,
            top = childPadding.top + 80.dp,
            bottom = 80.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(LibraryShelfSpacing),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            LibraryPersonalizedHeader(userName = userName)
        }

        itemsIndexed(shelves, key = { _, shelf -> shelf.id.name }) { index, shelf ->
            LibraryShelfRow(
                shelf = shelf,
                loadingMore = loadingShelfId == shelf.id,
                onMovieClick = onMovieClick,
                onLibraryItemClick = onLibraryItemClick,
                onLoadMore = { onLoadMore(shelf) },
                onBrowseHome = onBrowseHome,
                onBrowseStore = onBrowseStore,
                firstItemFocusRequester = if (index == 0) firstContentFocusRequester else null,
                leftFocusRequester = if (index == 0) sidebarFocusRequester else null,
            )
        }
    }
}

@Composable
private fun LibraryPersonalizedHeader(userName: String?) {
    val title = when {
        !userName.isNullOrBlank() -> "$userName's library"
        else -> "My library"
    }
    val subtitle = if (!userName.isNullOrBlank()) {
        "Your watch history, saves, and unlocks — all in one place"
    } else {
        "Everything you have watched or unlocked"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.8).sp,
            maxLines = 2,
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.52f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 5.dp),
            maxLines = 2,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LibraryShelfRow(
    shelf: LibraryShelf,
    loadingMore: Boolean,
    onMovieClick: (String) -> Unit,
    onLibraryItemClick: (LibraryItem) -> Unit,
    onLoadMore: () -> Unit,
    onBrowseHome: () -> Unit,
    onBrowseStore: () -> Unit,
    firstItemFocusRequester: FocusRequester? = null,
    leftFocusRequester: FocusRequester? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shelf.title,
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = LibraryShelfTitleSize,
                    letterSpacing = (-0.35).sp,
                )
                if (shelf.id == LibraryShelfId.ContinueWatching) {
                    Text(
                        text = "Pick up where you left off",
                        color = Color.White.copy(alpha = 0.52f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (shelf.items.isNotEmpty()) {
                Text(
                    text = shelf.total.takeIf { it > 0 }?.toString() ?: shelf.items.size.toString(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        if (shelf.items.isEmpty()) {
            LibraryShelfEmptyPlaceholder(
                shelfId = shelf.id,
                onBrowseHome = onBrowseHome,
                onBrowseStore = onBrowseStore,
                focusRequester = firstItemFocusRequester,
                leftFocusRequester = leftFocusRequester,
            )
        } else if (shelf.items.first().layout == LibraryCardLayout.Landscape) {
            MoviesRow(
                modifier = Modifier.focusGroup(),
                movieList = shelf.items.map { it.movie },
                title = null,
                itemDirection = ItemDirection.Horizontal,
                showItemTitle = true,
                onMovieSelected = { movie ->
                    shelf.items.firstOrNull { it.movie.id == movie.id }
                        ?.let { item ->
                            if (item.clickAction != LibraryClickAction.Nothing) {
                                onLibraryItemClick(item)
                            }
                        }
                        ?: onMovieClick(movie.id)
                },
                onViewMoreClick = if (shelf.hasMore) onLoadMore else null,
                firstItemFocusRequester = firstItemFocusRequester,
                leftFocusRequester = leftFocusRequester,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 24.dp),
                modifier = Modifier
                    .focusGroup()
                    .focusRestorer(),
            ) {
                itemsIndexed(shelf.items, key = { _, item -> "${shelf.id}-${item.movieId}" }) { index, item ->
                    LibraryItemCard(
                        item = item,
                        shelfId = shelf.id,
                        onClick = {
                            if (item.clickAction != LibraryClickAction.Nothing) {
                                onLibraryItemClick(item)
                            }
                        },
                        modifier = Modifier.then(
                            if (index == 0 && firstItemFocusRequester != null) {
                                Modifier
                                    .focusRequester(firstItemFocusRequester)
                                    .then(
                                        if (leftFocusRequester != null) {
                                            Modifier.focusProperties { left = leftFocusRequester }
                                        } else {
                                            Modifier
                                        },
                                    )
                            } else {
                                Modifier
                            },
                        ),
                    )
                }
                if (shelf.hasMore) {
                    item(key = "${shelf.id}-view-more") {
                        LibraryViewMoreCard(
                            loading = loadingMore,
                            onClick = onLoadMore,
                            height = LibraryPortraitArtHeight,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LibraryShelfEmptyPlaceholder(
    shelfId: LibraryShelfId,
    onBrowseHome: () -> Unit,
    onBrowseStore: () -> Unit,
    focusRequester: FocusRequester? = null,
    leftFocusRequester: FocusRequester? = null,
) {
    val copy = when (shelfId) {
        LibraryShelfId.ContinueWatching -> ShelfEmptyCopy(
            title = "Nothing to continue",
            hint = "Start watching and pick up where you left off",
            ctaLabel = "Browse home",
            onPress = onBrowseHome,
        )
        LibraryShelfId.Purchased -> ShelfEmptyCopy(
            title = "No owned titles yet",
            hint = "Buy a film or series and it will show up here",
            ctaLabel = "Go to store",
            onPress = onBrowseStore,
            showStoreIcon = true,
        )
        LibraryShelfId.ExpiredPurchases -> ShelfEmptyCopy(
            title = "No expired purchases",
            hint = "Titles you owned that expired will appear here",
            ctaLabel = "Go to store",
            onPress = onBrowseStore,
            showStoreIcon = true,
        )
        LibraryShelfId.Rented -> ShelfEmptyCopy(
            title = "No active rentals",
            hint = "Rent from the store to watch here",
            ctaLabel = "Go to store",
            onPress = onBrowseStore,
            showStoreIcon = true,
        )
        LibraryShelfId.RentedExpired -> ShelfEmptyCopy(
            title = "No expired rentals",
            hint = "Past rentals that expired will show up here",
            ctaLabel = "Go to store",
            onPress = onBrowseStore,
            showStoreIcon = true,
        )
        else -> return
    }

    Surface(
        onClick = copy.onPress,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.06f),
            focusedContainerColor = Color.White.copy(alpha = 0.12f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (focusRequester != null) {
                    Modifier
                        .focusRequester(focusRequester)
                        .then(
                            if (leftFocusRequester != null) {
                                Modifier.focusProperties { left = leftFocusRequester }
                            } else {
                                Modifier
                            },
                        )
                } else {
                    Modifier
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = copy.title,
                color = Color.White.copy(alpha = 0.88f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Text(
                text = copy.hint,
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = copy.ctaLabel,
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

private data class ShelfEmptyCopy(
    val title: String,
    val hint: String,
    val ctaLabel: String,
    val onPress: () -> Unit,
    val showStoreIcon: Boolean = false,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryItemCard(
    item: LibraryItem,
    shelfId: LibraryShelfId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (item.layout) {
        LibraryCardLayout.Portrait -> LibraryPortraitCard(
            item = item,
            shelfId = shelfId,
            onClick = onClick,
            modifier = modifier,
        )
        LibraryCardLayout.Landscape -> LibraryLandscapeCard(item = item, onClick = onClick, modifier = modifier)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryPortraitCard(
    item: LibraryItem,
    shelfId: LibraryShelfId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showBookmarkBadge = shelfId == LibraryShelfId.Bookmarks
    val unavailable = item.clickAction == LibraryClickAction.Nothing
    val cardGradient = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.45f to Color.Transparent,
                0.78f to Color.Black.copy(alpha = 0.55f),
                1f to Color.Black.copy(alpha = 0.92f),
            ),
        )
    }

    Column(
        modifier = modifier.width(LibraryPortraitWidth),
    ) {
        Surface(
            onClick = if (unavailable) ({}) else onClick,
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LibraryPortraitRadius)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFF1A1A1A),
                focusedContainerColor = Color(0xFF242424),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(LibraryPortraitArtHeight),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(BrewImageUrl.forPortraitCard(item.movie.posterUri))
                        .size(
                            BrewImageUrl.PORTRAIT_CARD_WIDTH,
                            BrewImageUrl.PORTRAIT_CARD_HEIGHT,
                        )
                        .crossfade(false)
                        .build(),
                    contentDescription = item.movie.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (unavailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Unavailable",
                            color = Color.White,
                            fontFamily = BrewTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cardGradient),
                )
                if (showBookmarkBadge && !unavailable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.58f))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_bookmark_filled),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Text(
                    text = item.movie.name,
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                )
                if (item.progressPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.14f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(item.progressPercent / 100f)
                                .height(4.dp)
                                .background(MovieDetailTokens.AccentYellow),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryLandscapeCard(
    item: LibraryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BrewLandscapeMovieCard(
        movie = item.movie.copy(watchProgressPercent = item.progressPercent),
        onClick = onClick,
        showTitle = true,
        modifier = modifier,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryViewMoreCard(
    loading: Boolean,
    onClick: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = if (loading) ({}) else onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LibraryPortraitRadius)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.08f),
            focusedContainerColor = Color.White.copy(alpha = 0.14f),
            contentColor = Color.White,
            focusedContentColor = Color.White,
        ),
        modifier = modifier
            .width(LibraryViewMoreWidth)
            .height(height),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (loading) {
                Text(
                    text = "Loading…",
                    color = MovieDetailTokens.AccentYellow,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .background(Color.White.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = "View more",
                    color = Color.White.copy(alpha = 0.88f),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(horizontal = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LibraryGuestState(
    onSignInClick: () -> Unit,
    focusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester?,
) {
    LibraryCenteredMessage(
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_library),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        title = "Sign in to access your library",
        subtitle = "Your watch history, saves, and unlocks will appear here",
        actionLabel = "Sign in",
        onAction = onSignInClick,
        focusRequester = focusRequester,
        sidebarFocusRequester = sidebarFocusRequester,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LibraryEmptyState(
    userName: String?,
    avatarUrl: String?,
    onBrowse: () -> Unit,
    focusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LibraryPersonalizedHeader(userName = userName)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Mastered the art of owning zero movies",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Start building your collection today.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )
        Spacer(modifier = Modifier.height(22.dp))
        LibraryPrimaryButton(
            label = "Browse movies",
            onClick = onBrowse,
            modifier = Modifier
                .focusRequester(focusRequester)
                .then(
                    if (sidebarFocusRequester != null) {
                        Modifier.focusProperties { left = sidebarFocusRequester }
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(999.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.92f),
        ),
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LibraryErrorState(
    message: String,
    onRetry: () -> Unit,
    focusRequester: FocusRequester,
    sidebarFocusRequester: FocusRequester?,
) {
    LibraryCenteredMessage(
        icon = null,
        title = "We couldn't load your library",
        subtitle = message,
        actionLabel = "Try again",
        onAction = onRetry,
        focusRequester = focusRequester,
        sidebarFocusRequester = sidebarFocusRequester,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LibraryCenteredMessage(
    icon: (@Composable () -> Unit)?,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    focusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.invoke()
        if (icon != null) Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )
        Spacer(modifier = Modifier.height(22.dp))
        LibraryPrimaryButton(
            label = actionLabel,
            onClick = onAction,
            modifier = Modifier.then(
                if (focusRequester != null) {
                    Modifier
                        .focusRequester(focusRequester)
                        .then(
                            if (sidebarFocusRequester != null) {
                                Modifier.focusProperties { left = sidebarFocusRequester }
                            } else {
                                Modifier
                            },
                        )
                } else {
                    Modifier
                },
            ),
        )
    }
}
