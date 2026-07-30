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

package com.google.jetstream.presentation.screens

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.jetstream.presentation.screens.categories.CategoryMovieListScreen
import com.google.jetstream.presentation.screens.movies.MovieDetailsScreen
import com.google.jetstream.presentation.screens.videoPlayer.VideoPlayerScreen

enum class Screens(
    private val args: List<String>? = null,
    val isTabItem: Boolean = false,
    val tabIcon: ImageVector? = null,
) {
    Profile(tabIcon = Icons.Outlined.Settings),
    Search(isTabItem = true, tabIcon = Icons.Outlined.Search),
    Home(isTabItem = true, tabIcon = Icons.Outlined.GridView),
    BrewPlus(isTabItem = true),
    Shorts(isTabItem = true, tabIcon = Icons.Outlined.Movie),
    Store(isTabItem = true, tabIcon = Icons.Outlined.Storefront),
    Favourites(isTabItem = true, tabIcon = Icons.Outlined.BookmarkBorder),
    CategoryMovieList(listOf(CategoryMovieListScreen.CategoryIdBundleKey)),
    MovieDetails(listOf(MovieDetailsScreen.MovieIdBundleKey)),
    MovieDetailsOverlay(listOf(MovieDetailsScreen.MovieIdBundleKey)),
    Dashboard,
    Auth,
    VideoPlayer(listOf(VideoPlayerScreen.MovieIdBundleKey));

    operator fun invoke(): String {
        val argList = StringBuilder()
        args?.let { nnArgs ->
            nnArgs.forEach { arg -> argList.append("/{$arg}") }
        }
        return name + argList
    }

    fun withArgs(vararg args: Any): String {
        val destination = StringBuilder(name)
        args.forEach { arg ->
            destination.append('/').append(Uri.encode(arg.toString()))
        }
        return destination.toString()
    }
}
