package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.theme.BrewTitle

/** Primary Brew hubs — matches vod-frontend Header pills. */
val BrewTopNavTabs = listOf(
    Screens.Home,
    Screens.Shorts,
    Screens.Store,
    Screens.Search,
    Screens.Favourites,
)

private val PillTrack = Color(0x99000000)
private val PillBorder = Color.White.copy(alpha = 0.1f)
private val PillShape = RoundedCornerShape(999.dp)
private val NavLabelStyle = TextStyle(
    fontFamily = BrewTitle,
    fontSize = 11.sp,
    lineHeight = 11.sp,
    letterSpacing = 1.1.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

/**
 * Floating Brew header — logo left, pill nav center, profile right.
 * Transparent so the Prime-style showcase can bleed underneath.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BrewTopHeader(
    selectedRoute: String?,
    onNavigateTo: (Screens) -> Unit,
    modifier: Modifier = Modifier,
    downFocusRequester: FocusRequester? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BrewHeaderOverlayHeight)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.brew_logo),
            contentDescription = "Brew",
            modifier = Modifier
                .height(26.dp)
                .width(68.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(PillShape)
                .background(PillTrack)
                .border(1.dp, PillBorder, PillShape)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrewTopNavTabs.forEach { screen ->
                val label = when (screen) {
                    Screens.Favourites -> "Library"
                    else -> screen.name
                }
                BrewNavPill(
                    label = label,
                    icon = navIcon(screen),
                    selected = selectedRoute == screen(),
                    onClick = { onNavigateTo(screen) },
                    downFocusRequester = downFocusRequester,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BrewNavIconButton(
            selected = selectedRoute == Screens.Profile(),
            onClick = { onNavigateTo(Screens.Profile) },
            contentDescription = "Settings",
            downFocusRequester = downFocusRequester,
            icon = {
                val icon = Screens.Profile.tabIcon
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Settings",
                        modifier = Modifier.size(15.dp),
                    )
                }
            },
        )
    }
}

private fun navIcon(screen: Screens): ImageVector? = when (screen) {
    Screens.Home -> Icons.Default.Home
    Screens.Shorts -> Icons.Default.VideoLibrary
    Screens.Store -> Icons.Default.ShoppingBag
    Screens.Search -> Icons.Default.Search
    else -> null
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BrewNavPill(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit,
    downFocusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val container by animateColorAsState(
        targetValue = when {
            focused || selected -> Color.White
            else -> Color.Transparent
        },
        animationSpec = tween(140),
        label = "pillBg",
    )
    val content by animateColorAsState(
        targetValue = when {
            focused || selected -> Color.Black
            else -> Color.White.copy(alpha = 0.82f)
        },
        animationSpec = tween(140),
        label = "pillFg",
    )

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(PillShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = container,
            focusedContainerColor = container,
            pressedContainerColor = container,
            contentColor = content,
            focusedContentColor = content,
            pressedContentColor = content,
        ),
        modifier = Modifier
            .height(28.dp)
            .onFocusChanged { focused = it.isFocused }
            .then(
                if (downFocusRequester != null) {
                    Modifier.focusProperties { down = downFocusRequester }
                } else {
                    Modifier
                }
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = content,
                    )
                }
                Text(
                    text = label.uppercase(),
                    color = content,
                    style = NavLabelStyle,
                    fontWeight = if (selected || focused) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BrewNavIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    downFocusRequester: FocusRequester? = null,
    icon: @Composable () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val container by animateColorAsState(
        targetValue = when {
            focused -> Color.White.copy(alpha = 0.18f)
            selected -> Color.White.copy(alpha = 0.1f)
            else -> PillTrack
        },
        animationSpec = tween(140),
        label = "iconBg",
    )
    val tint by animateColorAsState(
        targetValue = if (focused || selected) Color.White else Color.White.copy(alpha = 0.72f),
        animationSpec = tween(140),
        label = "iconTint",
    )

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = container,
            focusedContainerColor = container,
            contentColor = tint,
            focusedContentColor = tint,
        ),
        modifier = Modifier
            .size(34.dp)
            .border(1.dp, PillBorder, CircleShape)
            .onFocusChanged { focused = it.isFocused }
            .then(
                if (downFocusRequester != null) {
                    Modifier.focusProperties { down = downFocusRequester }
                } else {
                    Modifier
                }
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
    }
}
