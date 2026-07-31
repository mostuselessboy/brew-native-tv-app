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
import com.google.jetstream.presentation.common.BrewLandscapeMovieCard
import com.google.jetstream.presentation.common.ItemDirection
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.screens.movies.MovieDetailTokens
import com.google.jetstream.presentation.theme.BrewTitle

private val LibraryBackground = Color(0xFF0A0A0A)
private val LibraryPortraitWidth = 150.dp
private val LibraryPortraitRadius = 8.dp
private val LibraryShelfSpacing = 32.dp

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LibraryBackground),
    ) {
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
            top = childPadding.top + 12.dp,
            bottom = 80.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(LibraryShelfSpacing),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            LibraryPersonalizedHeader(userName = userName, avatarUrl = avatarUrl)
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
private fun LibraryPersonalizedHeader(userName: String?, avatarUrl: String?) {
    val context = LocalContext.current
    val title = when {
        !userName.isNullOrBlank() -> "$userName's library"
        else -> "My library"
    }
    val subtitle = if (!userName.isNullOrBlank()) {
        "Your watch history, saves, and unlocks — all in one place"
    } else {
        "Everything you have watched or unlocked"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x47FFC15E),
                                Color(0x1AFFFFFF),
                                Color(0x08FFFFFF),
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center,
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
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
                    fontSize = 22.sp,
                    letterSpacing = (-0.55).sp,
                )
                if (shelf.id == LibraryShelfId.ContinueWatching) {
                    Text(
                        text = "Pick up where you left off",
                        color = Color.White.copy(alpha = 0.52f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
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
                        ?.let(onLibraryItemClick)
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
                        onClick = { onLibraryItemClick(item) },
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
                            portrait = true,
                            focusRequester = null,
                            leftFocusRequester = null,
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
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(18.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.02f),
            focusedContainerColor = Color.White.copy(alpha = 0.06f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp),
            )
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
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (copy.showStoreIcon) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_shopping_bag),
                    contentDescription = null,
                    tint = MovieDetailTokens.AccentYellow,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .width(22.dp)
                        .height(25.dp),
                )
            }
            Text(
                text = copy.title,
                color = Color.White.copy(alpha = 0.88f),
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = copy.hint,
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = copy.ctaLabel,
                color = Color.Black,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (item.layout) {
        LibraryCardLayout.Portrait -> LibraryPortraitCard(item = item, onClick = onClick, modifier = modifier)
        LibraryCardLayout.Landscape -> LibraryLandscapeCard(item = item, onClick = onClick, modifier = modifier)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryPortraitCard(
    item: LibraryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(LibraryPortraitRadius)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF141414),
            focusedContainerColor = Color(0xFF1E1E1E),
        ),
        modifier = modifier
            .width(LibraryPortraitWidth)
            .aspectRatio(2f / 3f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(BrewImageUrl.forCard(item.movie.posterUri))
                    .crossfade(false)
                    .build(),
                contentDescription = item.movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.55f to Color.Black.copy(alpha = 0.55f),
                                1f to Color.Black.copy(alpha = 0.88f),
                            ),
                        ),
                    )
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    text = item.movie.name,
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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
    Column(modifier = modifier) {
        BrewLandscapeMovieCard(
            movie = item.movie,
            onClick = onClick,
            showTitle = true,
        )
        if (item.progressPercent > 0) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(com.google.jetstream.presentation.common.BrewLandscapeCardWidth)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.12f)),
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryViewMoreCard(
    loading: Boolean,
    onClick: () -> Unit,
    portrait: Boolean,
    focusRequester: FocusRequester? = null,
    leftFocusRequester: FocusRequester? = null,
) {
    val width = if (portrait) LibraryPortraitWidth else com.google.jetstream.presentation.common.BrewLandscapeCardWidth
    val aspect = if (portrait) 2f / 3f else 16f / 9f

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF141414),
            focusedContainerColor = Color(0xFF1E1E1E),
        ),
        modifier = Modifier
            .width(width)
            .aspectRatio(aspect)
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
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = if (loading) "Loading…" else "View more",
                color = if (loading) MovieDetailTokens.AccentYellow else Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
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
                    painter = painterResource(R.drawable.ic_lucide_bookmark),
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
        LibraryPersonalizedHeader(userName = userName, avatarUrl = avatarUrl)
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
