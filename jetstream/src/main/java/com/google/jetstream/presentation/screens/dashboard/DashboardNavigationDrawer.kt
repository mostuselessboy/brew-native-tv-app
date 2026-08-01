package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.util.BrewImageUrl
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import com.google.jetstream.R
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.theme.BrewTitle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

private const val WatchHiddenGemsUrl =
    "https://createstir.b-cdn.net/stir-static/watch-hidden-gems.png"

private val RailLogoHeight = 28.dp
private val RailLogoWidth = 44.dp
private val RailWordmarkHeight = 28.dp
private val RailWordmarkWidth = 44.dp

/** Side rail — pure black, white pill when focused/selected. */
private val RailBg = Color(0xFF000000)
private val RailContentBg = Color(0xFF000000)
private val RailSelectedPill = Color.White.copy(alpha = 0.12f)
private val RailItemShape = RoundedCornerShape(11.dp)
private val IconSize = 15.dp
private val StoreIconSize = 13.dp
private val RailItemSize = 40.dp
private const val RailFocusedScale = 1.12f

private data class BrewRailEntry(
    val screen: Screens,
    val iconRes: Int,
    val compactIcon: Boolean = false,
)

private val PrimaryRailEntries = listOf(
    BrewRailEntry(screen = Screens.Home, iconRes = R.drawable.ic_lucide_home),
    BrewRailEntry(screen = Screens.BrewPlus, iconRes = R.drawable.ic_lucide_crown),
    BrewRailEntry(screen = Screens.Shorts, iconRes = R.drawable.ic_lucide_shorts),
    BrewRailEntry(
        screen = Screens.Store,
        iconRes = R.drawable.ic_lucide_store,
        compactIcon = true,
    ),
    BrewRailEntry(screen = Screens.Search, iconRes = R.drawable.ic_lucide_search),
    BrewRailEntry(screen = Screens.Favourites, iconRes = R.drawable.ic_lucide_bookmark),
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DashboardNavigationDrawer(
    selectedRoute: String?,
    onNavigateTo: (Screens) -> Unit,
    modifier: Modifier = Modifier,
    contentFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    onRailFocus: (Screens, Boolean) -> Unit = { _, _ -> },
    onRailBlur: (Screens) -> Unit = {},
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(RailBg),
    ) {
        Column(
            modifier = Modifier
                .width(BrewRailWidth)
                .fillMaxHeight()
                .background(RailBg)
                .padding(vertical = 16.dp)
                .selectableGroup()
                .focusGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.brew_logo),
                    contentDescription = "Brew",
                    modifier = Modifier
                        .height(RailLogoHeight)
                        .width(RailLogoWidth)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit,
                )
                val pageName = when (selectedRoute) {
                    Screens.Home() -> "Home"
                    Screens.BrewPlus() -> "Brew Plus"
                    Screens.Shorts() -> "Shorts"
                    Screens.Store() -> "Store"
                    Screens.Search() -> "Search"
                    Screens.Favourites() -> "My Library"
                    Screens.Profile() -> "Profile"
                    else -> "Brew"
                }
                Text(
                    text = pageName,
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
            ) {
                PrimaryRailEntries.forEach { entry ->
                    BrewRailItem(
                        screen = entry.screen,
                        label = entry.screen.name,
                        selected = selectedRoute == entry.screen(),
                        iconRes = entry.iconRes,
                        iconSize = if (entry.compactIcon) StoreIconSize else IconSize,
                        onClick = { onNavigateTo(entry.screen) },
                        onRailFocus = onRailFocus,
                        onRailBlur = onRailBlur,
                        contentFocusRequester = contentFocusRequester,
                        sidebarFocusRequester = if (selectedRoute == entry.screen()) {
                            sidebarFocusRequester
                        } else {
                            null
                        },
                    )
                }
            }

            BrewRailItem(
                screen = Screens.Profile,
                label = "Profile",
                selected = selectedRoute == Screens.Profile(),
                iconRes = R.drawable.ic_lucide_profile,
                onClick = { onNavigateTo(Screens.Profile) },
                onRailFocus = onRailFocus,
                onRailBlur = onRailBlur,
                contentFocusRequester = contentFocusRequester,
                sidebarFocusRequester = if (selectedRoute == Screens.Profile()) {
                    sidebarFocusRequester
                } else {
                    null
                },
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(RailContentBg)
                .focusGroup(),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BrewRailItem(
    screen: Screens,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    iconSize: Dp = IconSize,
    onRailFocus: (Screens, Boolean) -> Unit = { _, _ -> },
    onRailBlur: (Screens) -> Unit = {},
    contentFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (focused) RailFocusedScale else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
        label = "railItemScale",
    )
    val pillColor = when {
        focused -> Color.White
        selected -> RailSelectedPill
        else -> Color.Transparent
    }
    val tint = when {
        focused -> Color.Black
        selected -> Color.White.copy(alpha = 0.92f)
        else -> Color.White.copy(alpha = 0.5f)
    }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RailItemShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = pillColor,
            focusedContainerColor = pillColor,
            pressedContainerColor = pillColor,
            contentColor = tint,
            focusedContentColor = tint,
            pressedContentColor = tint,
        ),
        modifier = Modifier
            .size(RailItemSize)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) {
                    onRailFocus(screen, selected)
                } else {
                    onRailBlur(screen)
                }
            }
            .then(
                if (sidebarFocusRequester != null) {
                    Modifier.focusRequester(sidebarFocusRequester)
                } else {
                    Modifier
                }
            )
            .then(
                if (contentFocusRequester != null) {
                    Modifier
                        .focusProperties { right = contentFocusRequester }
                        .onPreviewKeyEvent { event ->
                            val isRight = event.key == Key.DirectionRight ||
                                event.key == Key.NavigateNext
                            if (!isRight) return@onPreviewKeyEvent false
                            if (event.type == KeyEventType.KeyDown) {
                                runCatching { contentFocusRequester.requestFocus() }
                            }
                            true
                        }
                } else {
                    Modifier
                }
            ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
