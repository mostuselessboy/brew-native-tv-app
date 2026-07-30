package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.google.jetstream.presentation.screens.Screens

/** Side-rail tab order — drives left/right slide direction. */
val RailTabRoutes = listOf(
    Screens.Home(),
    Screens.BrewPlus(),
    Screens.Shorts(),
    Screens.Store(),
    Screens.Search(),
    Screens.Favourites(),
    Screens.Profile(),
)

private const val TabSlideFraction = 0.28f

fun tabIndex(route: String): Int = RailTabRoutes.indexOf(route)

/** Route + explicit slide direction so every tab change animates reliably. */
data class TabNavTarget(
    val route: String,
    val slideDirection: Int = 1,
)

fun tabNavTargetFor(route: String, previousRoute: String): TabNavTarget {
    val from = tabIndex(previousRoute)
    val to = tabIndex(route)
    val direction = when {
        from < 0 || to < 0 || from == to -> 1
        to > from -> 1
        else -> -1
    }
    return TabNavTarget(route = route, slideDirection = direction)
}

fun AnimatedContentTransitionScope<TabNavTarget>.dashboardTabTransition(): ContentTransform {
    if (initialState.route == targetState.route) {
        return fadeIn(tween(1)) togetherWith fadeOut(tween(1))
    }
    val step = targetState.slideDirection
    return (
        slideInHorizontally(
            animationSpec = tween(80),
            initialOffsetX = { width -> (width * TabSlideFraction * step).toInt() },
        ) + fadeIn(tween(60))
        ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(80),
            targetOffsetX = { width -> (-width * TabSlideFraction * step).toInt() },
        ) + fadeOut(tween(50))
        )
}
