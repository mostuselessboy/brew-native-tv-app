package com.google.jetstream.presentation.screens.home

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.google.jetstream.data.util.ShowcaseCta
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.google.jetstream.presentation.common.ShowcaseHeroBackdrop
import com.google.jetstream.presentation.common.ShowcaseHeroFrame
import com.google.jetstream.presentation.common.ShowcaseHeroMetaRow
import com.google.jetstream.presentation.common.ShowcaseHeroStyles
import com.google.jetstream.presentation.common.showcaseInfoLine
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.utils.Padding

private const val BrewPlusWordmark =
    "https://createstir.b-cdn.net/stir-static/brew%2B.webp"

private val BannerScrim = Color(0xFF000000)
private val ShowcaseTitleStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Bold,
    fontSize = 42.sp,
    lineHeight = 40.sp,
    letterSpacing = (-2.4).sp,
)
private val ShowcaseMetaStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    letterSpacing = 0.5.sp,
)
private val ShowcaseDescStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 17.sp,
    fontWeight = FontWeight.Medium,
)
private val ShowcaseCtaPrimaryStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 16.sp,
    letterSpacing = (-0.35).sp,
)
private val ShowcaseCtaSecondaryStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 15.sp,
    letterSpacing = (-0.25).sp,
)
private val ShowcaseComingSoonHintStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 13.sp,
    letterSpacing = (-0.15).sp,
)
private val ShowcaseButtonWidth = 168.dp
private val ShowcaseButtonShape = RoundedCornerShape(12.dp)
private val ShowcaseButtonHeight = 40.dp
private val ShowcaseButtonPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
private val ShowcasePrimaryUnfocusedColor = Color(0xCC292628)
private val ShowcaseSecondaryUnfocusedColor = Color(0xB3242220)
private val ShowcaseSecondaryGlassColor = Color(0x1FFFFFFF)
private const val ShowcaseButtonFocusedScale = 1.06f
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
    sidebarFocusRequester: FocusRequester? = null,
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
    LaunchedEffect(slideIndex) {
        if (lastFocusedSlide != slideIndex) {
            runCatching { primaryFocus.requestFocus() }
            lastFocusedSlide = slideIndex
        }
    }

    ShowcaseHeroFrame(
        padding = padding,
        modifier = modifier.focusGroup(),
        showBrewPlus = activeMovie.showBrewPlus,
        alsoInStore = activeMovie.showStore,
        backdrop = {
            AnimatedContent(
                targetState = slideIndex,
                transitionSpec = {
                    (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            initialScale = 1.07f,
                            animationSpec = tween(380, easing = FastOutSlowInEasing),
                        ))
                        .togetherWith(
                            fadeOut(tween(140)) +
                                scaleOut(targetScale = 1.02f, animationSpec = tween(180)),
                        )
                },
                label = "showcaseBackdrop",
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                ShowcaseHeroBackdrop(
                    posterUri = movies[index].posterUri,
                    contentDescription = movies[index].name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
        overlay = {
            PrimeCarouselIndicator(
                itemCount = movies.size,
                activeItemIndex = slideIndex,
            )
        },
        bottomContent = {
            AnimatedContent(
                targetState = slideIndex,
                transitionSpec = {
                    (fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) {
                            (it * 0.08f).toInt().coerceAtLeast(28)
                        })
                        .togetherWith(
                            fadeOut(tween(120)) +
                                slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) {
                                    -(it * 0.06f).toInt().coerceAtLeast(20)
                                },
                        )
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
                sidebarFocusRequester = sidebarFocusRequester,
                showBrewPlus = activeMovie.showBrewPlus,
                onOpen = { openFeatured(activeMovie) },
                movie = activeMovie,
            )
        },
    )
}

@Composable
private fun ShowcaseCopy(movie: Movie) {
    Column {
        Text(
            text = movie.name,
            color = Color.White,
            style = ShowcaseHeroStyles.Title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        ShowcaseHeroMetaRow(
            infoLine = showcaseInfoLine(movie),
            showStore = movie.showStore,
        )

        if (movie.description.isNotBlank()) {
            Text(
                text = movie.description,
                color = Color.White.copy(alpha = 0.78f),
                style = ShowcaseHeroStyles.Description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
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
    sidebarFocusRequester: FocusRequester?,
    showBrewPlus: Boolean,
    onOpen: () -> Unit,
    movie: Movie,
) {
    var focusEnteredAt by remember { mutableLongStateOf(0L) }
    val primaryCta = ShowcaseCta.primaryCta(movie)

    if (movie.isComingSoon) {
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
                        if (it.isFocused) focusEnteredAt = SystemClock.uptimeMillis()
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
                colors = showcasePrimaryButtonColors(),
                fixedHeight = ShowcaseButtonHeight,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bell_filled),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = primaryCta.label,
                    style = ShowcaseCtaPrimaryStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            ShowcaseFocusButton(
                onClick = onOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(secondaryFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) focusEnteredAt = SystemClock.uptimeMillis()
                    }
                    .blockHorizontalKeysAfterFocusEntry(focusEnteredAt)
                    .focusProperties {
                        up = primaryFocusRequester
                        if (slideIndex == 0 && sidebarFocusRequester != null) {
                            left = sidebarFocusRequester
                        }
                    }
                    .showcaseSlideKeys(
                        activeIndex = slideIndex,
                        itemCount = itemCount,
                        onIndexChange = onSlideChange,
                    ),
            colors = showcaseSecondaryButtonColors(),
            fixedHeight = ShowcaseButtonHeight,
        ) {
            Text(
                text = stringResource(R.string.more_info),
                style = ShowcaseCtaPrimaryStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        }
        return
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
                    if (it.isFocused) focusEnteredAt = SystemClock.uptimeMillis()
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
            colors = showcasePrimaryButtonColors(),
            fixedHeight = ShowcaseButtonHeight,
        ) {
            Text(text = primaryCta.label, style = ShowcaseCtaPrimaryStyle)
        }

        ShowcaseFocusButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(secondaryFocusRequester)
                .onFocusChanged {
                    if (it.isFocused) focusEnteredAt = SystemClock.uptimeMillis()
                }
                .blockHorizontalKeysAfterFocusEntry(focusEnteredAt)
                .focusProperties {
                    up = primaryFocusRequester
                    if (slideIndex == 0 && sidebarFocusRequester != null) {
                        left = sidebarFocusRequester
                    }
                }
                .showcaseSlideKeys(
                    activeIndex = slideIndex,
                    itemCount = itemCount,
                    onIndexChange = onSlideChange,
                ),
            colors = showcaseSecondaryButtonColors(),
            fixedHeight = ShowcaseButtonHeight,
        ) {
            Text(
                text = stringResource(R.string.more_info),
                style = ShowcaseCtaPrimaryStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun showcasePrimaryButtonColors(): ButtonColors = ButtonDefaults.colors(
    containerColor = ShowcasePrimaryUnfocusedColor,
    contentColor = Color.White,
    focusedContainerColor = Color.White,
    focusedContentColor = Color.Black,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun showcaseSecondaryButtonColors(): ButtonColors = ButtonDefaults.colors(
    containerColor = ShowcaseSecondaryUnfocusedColor,
    contentColor = Color.White,
    focusedContainerColor = Color.White,
    focusedContentColor = Color.Black,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun showcaseSecondaryGlassButtonColors(): ButtonColors = ButtonDefaults.colors(
    containerColor = ShowcaseSecondaryGlassColor,
    contentColor = Color.White,
    focusedContainerColor = Color.White,
    focusedContentColor = Color.Black,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ShowcaseFocusButton(
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
    fixedHeight: Dp = ShowcaseButtonHeight,
    content: @Composable RowScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) ShowcaseButtonFocusedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 340f,
        ),
        label = "showcaseBtnScale",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = fixedHeight, max = fixedHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused },
        contentPadding = ShowcaseButtonPadding,
        shape = ButtonDefaults.shape(shape = ShowcaseButtonShape),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        colors = colors,
        content = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        },
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
    val width by animateDpAsState(
        targetValue = if (isActive) 16.dp else 4.dp,
        animationSpec = tween(260),
        label = "dotWidth",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.35f,
        animationSpec = tween(260),
        label = "dotAlpha",
    )
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
