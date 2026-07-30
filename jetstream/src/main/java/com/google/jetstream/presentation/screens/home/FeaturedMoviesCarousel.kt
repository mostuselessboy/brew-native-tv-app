package com.google.jetstream.presentation.screens.home

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.common.showcaseInfoLine
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.utils.Padding

/** Hero height — Netflix-style with first tray peeking below. */
val ShowcaseHeight = 380.dp

private const val BrewPlusWordmark =
    "https://createstir.b-cdn.net/stir-static/brew%2B.webp"

private val BannerScrim = Color(0xFF000000)
private val ShowcaseTitleStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Bold,
    fontSize = 38.sp,
    lineHeight = 34.sp,
    letterSpacing = (-2.8).sp,
)
private val ShowcaseButtonWidth = 168.dp
private val ShowcaseButtonShape = RoundedCornerShape(8.dp)
private val ShowcaseButtonHeight = 36.dp
private val ShowcaseButtonPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp)
private const val HorizontalKeyGraceMs = 320L

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FeaturedMoviesCarousel(
    movies: List<Movie>,
    padding: Padding,
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier,
    primaryFocusRequester: FocusRequester? = null,
    secondaryFocusRequester: FocusRequester? = null,
    downFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    focusEnabled: Boolean = true,
    onShowcaseFocused: () -> Unit = {},
) {
    if (movies.isEmpty()) return

    var slideIndex by rememberSaveable { mutableIntStateOf(0) }
    val localPrimaryFocus = remember { FocusRequester() }
    val localSecondaryFocus = remember { FocusRequester() }
    val primaryFocus = primaryFocusRequester ?: localPrimaryFocus
    val secondaryFocus = secondaryFocusRequester ?: localSecondaryFocus
    val activeMovie = movies[slideIndex]

    fun openFeatured(movie: Movie) {
        onMovieClick(movie)
    }

    @Suppress("UNUSED_PARAMETER")
    val unusedPlayerNav = goToVideoPlayer

    var lastFocusedSlide by remember { mutableIntStateOf(slideIndex) }
    LaunchedEffect(slideIndex, focusEnabled) {
        if (!focusEnabled) return@LaunchedEffect
        if (lastFocusedSlide != slideIndex) {
            runCatching { primaryFocus.requestFocus() }
            lastFocusedSlide = slideIndex
        }
    }

    Box(
        modifier = modifier
            .focusGroup()
            .focusProperties {
                canFocus = false
            },
    ) {
        AnimatedContent(
            targetState = slideIndex,
            transitionSpec = {
                fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
            },
            label = "showcaseBackdrop",
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            ShowcaseBackdrop(movie = movies[index])
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = padding.start,
                    end = padding.end,
                    bottom = 14.dp,
                )
                .widthIn(max = 460.dp)
                .fillMaxWidth(0.56f),
            verticalArrangement = Arrangement.Bottom,
        ) {
            AnimatedContent(
                targetState = slideIndex,
                transitionSpec = {
                    fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                },
                label = "showcaseCopy",
            ) { index ->
                ShowcaseCopy(movie = movies[index])
            }

            ShowcaseActionButtons(
                slideIndex = slideIndex,
                itemCount = movies.size,
                onSlideChange = { slideIndex = it },
                primaryFocusRequester = primaryFocus,
                secondaryFocusRequester = secondaryFocus,
                downFocusRequester = downFocusRequester,
                sidebarFocusRequester = sidebarFocusRequester,
                showBrewPlus = activeMovie.showBrewPlus,
                onOpen = { openFeatured(activeMovie) },
                onShowcaseFocused = onShowcaseFocused,
            )
        }

        if (activeMovie.showBrewPlus) {
            ShowcaseBrewPlusRow(
                alsoInStore = activeMovie.showStore,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = padding.end,
                        bottom = 18.dp,
                    ),
            )
        }

        PrimeCarouselIndicator(
            itemCount = movies.size,
            activeItemIndex = slideIndex,
        )
    }
}

@Composable
private fun ShowcaseBackdrop(movie: Movie) {
    val contentBg = Color.Black
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(BrewImageUrl.forShowcase(movie.posterUri))
                .size(BrewImageUrl.SHOWCASE_WIDTH, BrewImageUrl.SHOWCASE_HEIGHT)
                .crossfade(false)
                .build(),
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Black.copy(alpha = 0.35f),
                                0.5f to Color.Transparent,
                                0.78f to Color.Black.copy(alpha = 0.45f),
                                1f to Color.Black,
                            ),
                        ),
                    )
                },
        )

        // Left panel — blends image into text column (Netflix-style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to contentBg,
                                0.18f to contentBg.copy(alpha = 0.98f),
                                0.32f to contentBg.copy(alpha = 0.88f),
                                0.46f to contentBg.copy(alpha = 0.55f),
                                0.56f to contentBg.copy(alpha = 0.22f),
                                0.64f to Color.Transparent,
                                1f to Color.Transparent,
                            ),
                        ),
                    )
                },
        )

        // Soft seam where hero meets the left rail edge
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.12f)
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to contentBg,
                            1f to Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun ShowcaseCopy(movie: Movie) {
    Column {
        Text(
            text = movie.name,
            color = Color.White,
            style = ShowcaseTitleStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        val info = showcaseInfoLine(movie)
        if (info.isNotBlank() || movie.showStore) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                if (movie.showStore) {
                    Icon(
                        painter = painterResource(R.drawable.ic_brew_store),
                        contentDescription = null,
                        tint = Color(0xFFE6D391),
                        modifier = Modifier.size(9.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
                if (info.isNotBlank()) {
                    Text(
                        text = info.uppercase(),
                        color = Color.White.copy(alpha = 0.78f),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (movie.description.isNotBlank()) {
            Text(
                text = movie.description,
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
private fun ShowcaseActionButtons(
    slideIndex: Int,
    itemCount: Int,
    onSlideChange: (Int) -> Unit,
    primaryFocusRequester: FocusRequester,
    secondaryFocusRequester: FocusRequester,
    downFocusRequester: FocusRequester?,
    sidebarFocusRequester: FocusRequester?,
    showBrewPlus: Boolean,
    onOpen: () -> Unit,
    onShowcaseFocused: () -> Unit,
) {
    var focusEnteredAt by remember { mutableLongStateOf(0L) }
    val primaryLabel = if (showBrewPlus) {
        stringResource(R.string.subscribe)
    } else {
        stringResource(R.string.watch_now)
    }

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .width(ShowcaseButtonWidth)
            .focusGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShowcaseFocusButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(primaryFocusRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        focusEnteredAt = SystemClock.uptimeMillis()
                        onShowcaseFocused()
                    }
                }
                .blockHorizontalKeysAfterFocusEntry(focusEnteredAt)
                .focusProperties {
                    down = secondaryFocusRequester
                    if (slideIndex == 0 && sidebarFocusRequester != null) {
                        left = sidebarFocusRequester
                    }
                }
                .showcaseSlideKeys(
                    activeIndex = slideIndex,
                    itemCount = itemCount,
                    onIndexChange = onSlideChange,
                ),
            colors = ButtonDefaults.colors(
                containerColor = Color.White,
                contentColor = Color.Black,
                focusedContainerColor = Color.White,
                focusedContentColor = Color.Black,
            ),
        ) {
            Text(
                text = primaryLabel,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = (-0.35).sp,
            )
        }

        ShowcaseFocusButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(secondaryFocusRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        focusEnteredAt = SystemClock.uptimeMillis()
                        onShowcaseFocused()
                    }
                }
                .blockHorizontalKeysAfterFocusEntry(focusEnteredAt)
                .focusProperties {
                    up = primaryFocusRequester
                    down = downFocusRequester ?: FocusRequester.Default
                    if (slideIndex == 0 && sidebarFocusRequester != null) {
                        left = sidebarFocusRequester
                    }
                }
                .showcaseSlideKeys(
                    activeIndex = slideIndex,
                    itemCount = itemCount,
                    onIndexChange = onSlideChange,
                ),
            colors = ButtonDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.22f),
                focusedContentColor = Color.White,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.more_info),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = (-0.25).sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ShowcaseFocusButton(
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ShowcaseButtonHeight),
        contentPadding = ShowcaseButtonPadding,
        shape = ButtonDefaults.shape(shape = ShowcaseButtonShape),
        scale = ButtonDefaults.scale(focusedScale = 1.06f),
        colors = colors,
        content = content,
    )
}

/**
 * Swallows stray horizontal KeyUp events right after focus enters from the rail.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.blockHorizontalKeysAfterFocusEntry(
    focusEnteredAt: Long,
): Modifier = onPreviewKeyEvent { event ->
    val isHorizontal = event.key == Key.DirectionLeft ||
        event.key == Key.DirectionRight ||
        event.key == Key.NavigatePrevious ||
        event.key == Key.NavigateNext
    if (!isHorizontal) return@onPreviewKeyEvent false
    SystemClock.uptimeMillis() - focusEnteredAt < HorizontalKeyGraceMs
}

/**
 * Slide keys only on focused buttons (KeyDown) — never on a parent Column, so
 * RIGHT from the rail cannot advance slides after focus enters the showcase.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.showcaseSlideKeys(
    activeIndex: Int,
    itemCount: Int,
    onIndexChange: (Int) -> Unit,
): Modifier = onKeyEvent { event ->
    if (event.nativeKeyEvent.action != KeyEvent.ACTION_DOWN) return@onKeyEvent false
    when (event.nativeKeyEvent.keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT -> {
            if (activeIndex > 0) {
                onIndexChange(activeIndex - 1)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT -> {
            if (activeIndex < itemCount - 1) {
                onIndexChange(activeIndex + 1)
                true
            } else {
                false
            }
        }
        else -> false
    }
}

@Composable
private fun ShowcaseBrewPlusRow(
    alsoInStore: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(
                if (alsoInStore) {
                    R.string.also_included_in_brew_plus
                } else {
                    R.string.included_in_brew_plus
                }
            ),
            color = Color.White.copy(alpha = 0.55f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = (-0.45).sp,
            lineHeight = 11.sp,
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(BrewPlusWordmark)
                .size(220, 44)
                .build(),
            contentDescription = "Brew Plus",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(9.dp)
                .width(34.dp),
        )
    }
}

@Composable
private fun BoxScope.PrimeCarouselIndicator(
    itemCount: Int,
    activeItemIndex: Int,
) {
    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .align(Alignment.BottomCenter)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(itemCount) { index ->
            AnimatedCarouselDot(isActive = index == activeItemIndex)
        }
    }
}

@Composable
private fun AnimatedCarouselDot(isActive: Boolean) {
    val width = if (isActive) 16.dp else 4.dp
    val alpha = if (isActive) 1f else 0.35f
    Box(
        modifier = Modifier
            .height(4.dp)
            .width(width)
            .background(
                Color.White.copy(alpha = alpha),
                RoundedCornerShape(50),
            ),
    )
}
