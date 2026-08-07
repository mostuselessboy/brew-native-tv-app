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

package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.jetstream.presentation.theme.BrewTitle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.theme.JetStreamCardShape

val TopBarTabs = Screens.entries.toList().filter { it.isTabItem && it != Screens.Search }

// +2 for ProfileTab, SearchTab
val TopBarFocusRequesters = List(size = TopBarTabs.size + 2) { FocusRequester() }

val SearchTopBarFocusRequester = TopBarFocusRequesters[1]
val ProfileTopBarFocusRequester = TopBarFocusRequesters[0]

val SEARCH_SCREEN_INDEX = -1
val PROFILE_SCREEN_INDEX = -2

private fun getScreenIcon(screen: Screens): Int = when(screen) {
    Screens.Search -> R.drawable.ic_lucide_search
    Screens.Home -> R.drawable.ic_lucide_home
    Screens.BrewPlus -> R.drawable.ic_lucide_crown
    Screens.Shorts -> R.drawable.ic_lucide_shorts
    Screens.Store -> R.drawable.ic_lucide_store
    Screens.Favourites -> R.drawable.ic_lucide_library
    else -> R.drawable.ic_lucide_home
}

private fun Modifier.dpadDownFocus(focusRequester: FocusRequester?): Modifier =
    if (focusRequester != null) {
        this
            .focusProperties { down = focusRequester }
            .onPreviewKeyEvent { event ->
                if (event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown) {
                    runCatching { focusRequester.requestFocus() }
                    true
                } else false
            }
    } else this

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DashboardTopBar(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    contentFocusRequester: FocusRequester? = null,
    avatarUrl: String? = null,
    screens: List<Screens> = TopBarTabs,
    focusRequesters: List<FocusRequester> = TopBarFocusRequesters,
    onScreenSelection: (screen: Screens) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val customColorScheme = MaterialTheme.colorScheme.copy(
        // The TabRow indicator uses inverseSurface for the pill
        inverseSurface = Color.White,
        inverseOnSurface = Color.Black
    )
    val pillContainerShape = RoundedCornerShape(50.dp)
    MaterialTheme(colorScheme = customColorScheme) {
        Box(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 32.dp, end = 32.dp, bottom = 8.dp)
                    // Transparent — no background
                    .focusRestorer(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Brew Logo + Watch hidden gems subtitle ──────────────────────
                com.google.jetstream.presentation.common.BrewBrandLogo()

                Spacer(modifier = Modifier.weight(1f))

                // ── Centered TabRow in dark pill container ───────────────────────
                var isTabRowFocused by remember { mutableStateOf(false) }
                var searchFocused by remember { mutableStateOf(false) }
                var profileFocused by remember { mutableStateOf(false) }
                val isExternalFocused = !isTabRowFocused && !searchFocused && !profileFocused

                // Pill color: full white when a tab is focused, dim 0.40f ghost when external focus
                val pillColor by androidx.compose.animation.animateColorAsState(
                    targetValue = when {
                        isExternalFocused -> Color(0xFF4A4A4A)
                        else -> Color.White
                    },
                    label = "pillColor"
                )
                val pillContainerBg by androidx.compose.animation.animateColorAsState(
                    targetValue = Color(0xFF2A2A2A),
                    label = "pillContainerBg"
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = pillContainerBg,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(4.dp)
                ) {
                val safeTabRowIndex = if (selectedTabIndex in screens.indices) selectedTabIndex else 0
                TabRow(
                    modifier = Modifier
                        .onFocusChanged {
                            isTabRowFocused = it.isFocused || it.hasFocus
                        },
                    selectedTabIndex = safeTabRowIndex,
                    indicator = { tabPositions, _ ->
                        if (selectedTabIndex in tabPositions.indices) {
                            DashboardTopBarItemIndicator(
                                currentTabPosition = tabPositions[selectedTabIndex],
                                anyTabFocused = isTabRowFocused,
                                activeColor = pillColor,
                                shape = RoundedCornerShape(50.dp)
                            )
                        }
                    },
                    separator = { Spacer(modifier = Modifier.width(0.dp)) }
                ) {
                    screens.forEachIndexed { index, screen ->
                        key(index) {
                            val isSelected = index == selectedTabIndex
                            val textColor = when {
                                isSelected && !isExternalFocused -> Color.Black
                                else -> Color.White
                            }
                            val isLastTab = index == screens.lastIndex

                            Tab(
                                modifier = Modifier
                                    .focusRequester(focusRequesters[index + 2])
                                    .focusProperties {
                                        if (isLastTab) {
                                            right = SearchTopBarFocusRequester
                                        }
                                    }
                                    .dpadDownFocus(contentFocusRequester),
                                selected = isSelected,
                                onFocus = { onScreenSelection(screen) },
                                onClick = {
                                    if (contentFocusRequester != null) {
                                        runCatching { contentFocusRequester.requestFocus() }
                                    } else {
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                },
                            ) {
                                val isBrewPlus = screen == Screens.BrewPlus
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(32.dp)
                                        .padding(horizontal = 14.dp)
                                ) {
                                    if (isBrewPlus) {
                                        Image(
                                            painter = painterResource(
                                                if (isSelected && !isExternalFocused) R.drawable.brewplus_wordmark_black
                                                else R.drawable.brewplus_wordmark_gold
                                            ),
                                            contentDescription = "brew+",
                                            modifier = Modifier.height(11.dp),
                                            contentScale = ContentScale.Fit,
                                        )
                                    } else {
                                        val label = when (screen) {
                                            Screens.Favourites -> "My Library"
                                            else -> screen()
                                        }
                                        Text(
                                            text = label,
                                            style = TextStyle(
                                                color = textColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = (-0.2).sp,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } // end TabRow
                } // end dark pill Box

                Spacer(modifier = Modifier.weight(1f))

                // ── Search + Profile pills ───────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isSearchActive = selectedTabIndex == SEARCH_SCREEN_INDEX
                    val searchBg = when {
                        searchFocused -> Color.White
                        isSearchActive && isExternalFocused -> Color(0xFF4A4A4A)
                        isSearchActive -> Color.White
                        else -> Color.Transparent
                    }
                    val searchIconTint = when {
                        searchFocused -> Color.Black
                        isSearchActive && isExternalFocused -> Color.White
                        isSearchActive -> Color.Black
                        else -> Color.White
                    }
                    val lastTabRequester = focusRequesters.getOrNull(screens.lastIndex + 2)

                    IconButton(
                        onClick = {
                            onScreenSelection(Screens.Search)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = searchBg,
                                shape = RoundedCornerShape(50.dp)
                            )
                            .focusRequester(focusRequesters[1])
                            .focusProperties {
                                if (lastTabRequester != null) left = lastTabRequester
                                right = ProfileTopBarFocusRequester
                            }
                            .dpadDownFocus(contentFocusRequester)
                            .onFocusChanged {
                                searchFocused = it.isFocused
                                if (it.isFocused) {
                                    onScreenSelection(Screens.Search)
                                }
                            }
                    ) {
                        Icon(
                            painter = painterResource(getScreenIcon(Screens.Search)),
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp),
                            tint = searchIconTint
                        )
                    }

                    UserAvatar(
                        modifier = Modifier
                            .size(28.dp)
                            .focusRequester(focusRequesters[0])
                            .focusProperties {
                                left = SearchTopBarFocusRequester
                            }
                            .dpadDownFocus(contentFocusRequester)
                            .onFocusChanged {
                                profileFocused = it.isFocused
                                if (it.isFocused) {
                                    onScreenSelection(Screens.Profile)
                                }
                            }
                            .semantics {
                                contentDescription = StringConstants.Composable.ContentDescription.UserAvatar
                            },
                        selected = selectedTabIndex == PROFILE_SCREEN_INDEX,
                        isExternalFocused = isExternalFocused,
                        avatarUrl = avatarUrl,
                        onClick = {
                            onScreenSelection(Screens.Profile)
                        }
                    )
                }
            }
        }
    }
}
