package com.google.jetstream.presentation.screens.profile

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.JetStreamTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen(
    openSignInPhone: () -> Unit = {},
    openSignInEmail: () -> Unit = {},
    sidebarFocusRequester: FocusRequester? = null,
    contentFocusRequester: FocusRequester? = null,
    isTabVisible: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0)
    sidebarWidthFraction: Float = 0.32f
) {
    val childPadding = rememberChildPadding()
    val fallbackFocusRequester = remember { FocusRequester() }
    val activePanelFocusRequester = contentFocusRequester ?: fallbackFocusRequester

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Top orange wash gradient: rgba(255,123,0,0.19) fading down to transparent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x30FF7B00), // rgba(255, 123, 0, 0.19)
                            Color(0x0AFF7B00), // rgba(255, 123, 0, 0.04)
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = childPadding.start, vertical = childPadding.top),
        ) {
            AccountsSection(
                onSignInPhone = openSignInPhone,
                onSignInEmail = openSignInEmail,
                panelFocusRequester = activePanelFocusRequester,
                sidebarFocusRequester = sidebarFocusRequester,
            )
        }
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
fun ProfileScreenPreview() {
    JetStreamTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            ProfileScreen()
        }
    }
}
