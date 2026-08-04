package com.google.jetstream.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.jetstream.data.util.resolveUserAvatarUrl
import com.google.jetstream.presentation.theme.BrewTitle

private val AccentYellow = Color(0xFFFFC15E)
private val GlassyCardBg = Color(0xB8070708) // rgba(7,7,8,0.72)
private val HairlineBorder = Color(0x1FFFFFFF) // rgba(255,255,255,0.12)
private val AvatarRingBg = Color(0xFF141414)
private const val DefaultAvatarUrl = "https://createstir.b-cdn.net/stir-static/watch-hidden-gems.png"

@Immutable
data class AccountsSectionData(
    val title: String,
    val value: String? = null,
    val onClick: () -> Unit = {},
    /** Read-only tiles (signed-in profile summary) are not focusable. */
    val focusable: Boolean = true,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AccountsSection(
    onSignInPhone: () -> Unit,
    onSignInEmail: () -> Unit,
    panelFocusRequester: FocusRequester? = null,
    sidebarFocusRequester: FocusRequester? = null,
    viewModel: AccountsViewModel = hiltViewModel(),
    authViewModel: com.google.jetstream.presentation.screens.auth.AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val authState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("India") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val defaultPanelFocus = remember { FocusRequester() }
    val panelFocus = panelFocusRequester ?: defaultPanelFocus
    val rightPanelFocus = remember { FocusRequester() }

    if (!authState.isSignedIn) {
        // UNAUTHENTICATED / GUEST MODE (matching FavouritesScreen / WatchlistScreen pattern)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(36.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Sign in to access your profile",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Your account details, purchases, and subscriptions will appear here",
                    fontFamily = BrewTitle,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
                )

                // Single Pillbox Sign In CTA with Centered Text
                CenteredPillButton(
                    label = "Sign In",
                    isPrimary = true,
                    onClick = onSignInPhone,
                    modifier = Modifier
                        .width(240.dp)
                        .focusRequester(panelFocus)
                        .focusProperties {
                            if (sidebarFocusRequester != null) left = sidebarFocusRequester
                        },
                )
            }
        }
    } else {
        // AUTHENTICATED MODE (Full Edit Profile Page matching reference photo)
        val user = authState.user
        val avatarUrl = remember(user) {
            resolveUserAvatarUrl(user) ?: DefaultAvatarUrl
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 36.dp, end = 36.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
        ) {
            // Left Column — Settings & Options Cards
            Column(
                modifier = Modifier
                    .width(440.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Edit Profile",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Account Group
                Text(
                    text = "Account",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassyCardBg, RoundedCornerShape(20.dp))
                        .border(1.dp, HairlineBorder, RoundedCornerShape(20.dp))
                        .padding(4.dp),
                ) {
                    Column {
                        ProfileOptionItem(
                            icon = Icons.Default.Person,
                            title = "CHANGE AVATAR",
                            subtitle = "Select a new profile picture",
                            onClick = { viewModel.refresh() },
                            modifier = Modifier
                                .focusRequester(panelFocus)
                                .focusProperties {
                                    if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                    right = rightPanelFocus
                                },
                        )
                        DividerLine()
                        ProfileOptionItem(
                            icon = Icons.Default.PlayArrow,
                            title = "MY LIBRARY",
                            subtitle = "View your rentals and purchases",
                            onClick = { },
                            modifier = Modifier.focusProperties {
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                right = rightPanelFocus
                            },
                        )
                        DividerLine()
                        ProfileOptionItem(
                            icon = Icons.Default.Star,
                            title = "MY SUBSCRIPTION",
                            subtitle = "See what you're subscribed to",
                            onClick = { },
                            modifier = Modifier.focusProperties {
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                right = rightPanelFocus
                            },
                        )
                        DividerLine()
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = "LOG OUT",
                            subtitle = "Sign out of your account",
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.focusProperties {
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                right = rightPanelFocus
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Preferences Group
                Text(
                    text = "Preferences",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassyCardBg, RoundedCornerShape(20.dp))
                        .border(1.dp, HairlineBorder, RoundedCornerShape(20.dp))
                        .padding(4.dp),
                ) {
                    Column {
                        ProfileOptionItem(
                            icon = Icons.Default.Public,
                            title = "COUNTRY",
                            subtitle = selectedCountry,
                            onClick = {
                                selectedCountry = if (selectedCountry == "India") "United States" else "India"
                            },
                            modifier = Modifier.focusProperties {
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                right = rightPanelFocus
                            },
                        )
                        DividerLine()
                        ProfileOptionItem(
                            icon = Icons.Default.Translate,
                            title = "LANGUAGE",
                            subtitle = selectedLanguage,
                            onClick = {
                                selectedLanguage = if (selectedLanguage == "English") "Hindi" else "English"
                            },
                            modifier = Modifier.focusProperties {
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                right = rightPanelFocus
                            },
                        )
                        DividerLine()
                        ProfileOptionItem(
                            icon = Icons.Default.Notifications,
                            title = "NOTIFICATIONS",
                            subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                            onClick = { notificationsEnabled = !notificationsEnabled },
                            modifier = Modifier.focusProperties {
                                if (sidebarFocusRequester != null) left = sidebarFocusRequester
                                right = rightPanelFocus
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "© 2026 Brew. All rights reserved.",
                    fontFamily = BrewTitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Right Column — User Profile Hero Avatar & Name
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    onClick = { viewModel.refresh() },
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f, pressedScale = 1f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.Transparent,
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        focusedContentColor = Color.White,
                    ),
                    modifier = Modifier
                        .size(160.dp)
                        .focusRequester(rightPanelFocus)
                        .focusProperties {
                            left = panelFocus
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AvatarRingBg, CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(72.dp),
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName ?: "Brew Member",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = user?.email ?: user?.phone ?: "Signed in",
                    fontFamily = BrewTitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    AccountsSectionDeleteDialog(
        showDialog = showDeleteDialog,
        onDismissRequest = { showDeleteDialog = false },
        modifier = Modifier.width(428.dp),
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.White,
            contentColor = Color.White,
            focusedContentColor = Color.Black,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF0F0F0F), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                Text(
                    text = subtitle,
                    fontFamily = BrewTitle,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 12.dp)
            .background(HairlineBorder),
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CenteredPillButton(
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f, pressedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isPrimary) AccentYellow else Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.White,
            contentColor = if (isPrimary) Color.Black else Color.White,
            focusedContentColor = Color.Black,
        ),
        modifier = modifier
            .height(46.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = label,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
