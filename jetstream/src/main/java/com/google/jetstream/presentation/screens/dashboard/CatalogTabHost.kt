package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.zIndex
import com.google.jetstream.data.remote.BrewPages
import com.google.jetstream.presentation.screens.Screens

private data class CatalogTab(
    val route: String,
    val page: String,
)

private val CatalogTabs = listOf(
    CatalogTab(route = Screens.Home(), page = BrewPages.HOME),
    CatalogTab(route = Screens.BrewPlus(), page = BrewPages.BREW_PLUS),
    CatalogTab(route = Screens.Shorts(), page = BrewPages.SHORTS),
    CatalogTab(route = Screens.Store(), page = BrewPages.STORE),
)

/**
 * All catalog tabs stay mounted (Profile-style) so switching is instant and scroll
 * position is preserved per tab. Only the visible tab accepts focus.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatalogTabHost(
    tabRoute: String,
    modifier: Modifier = Modifier,
    content: @Composable (page: String, isVisible: Boolean) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        CatalogTabs.forEach { tab ->
            val isVisible = tabRoute == tab.route
            key(tab.page) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (isVisible) 1f else 0f)
                        .focusProperties { canFocus = isVisible },
                ) {
                    content(tab.page, isVisible)
                }
            }
        }
    }
}
