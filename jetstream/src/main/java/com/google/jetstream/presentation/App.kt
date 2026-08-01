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

package com.google.jetstream.presentation

import androidx.compose.foundation.background
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.jetstream.presentation.screenBlackFadeIn
import com.google.jetstream.presentation.screenBlackFadeOut
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.categories.CategoryMovieListScreen
import com.google.jetstream.presentation.screens.collection.CollectionScreen
import com.google.jetstream.presentation.screens.auth.AuthScreenRoute
import com.google.jetstream.presentation.screens.dashboard.DashboardScreen
import com.google.jetstream.presentation.screens.movies.MovieDetailsScreen
import com.google.jetstream.presentation.screens.videoPlayer.VideoPlayerScreen

@Composable
fun App(
    onBackPressed: () -> Unit
) {

    val navController = rememberNavController()
    var isComingBackFromDifferentScreen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        NavHost(
            navController = navController,
            startDestination = Screens.Dashboard(),
            modifier = Modifier.fillMaxSize(),
            builder = {
            composable(
                route = Screens.CategoryMovieList(),
                enterTransition = { screenBlackFadeIn() },
                exitTransition = { screenBlackFadeOut() },
                popEnterTransition = { screenBlackFadeIn() },
                popExitTransition = { screenBlackFadeOut() },
                arguments = listOf(
                    navArgument(CategoryMovieListScreen.CategoryIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                CategoryMovieListScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    },
                    onMovieSelected = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        )
                    }
                )
            }
            composable(
                route = Screens.MovieDetails(),
                enterTransition = { screenBlackFadeIn() },
                exitTransition = { screenBlackFadeOut() },
                popEnterTransition = { screenBlackFadeIn() },
                popExitTransition = { screenBlackFadeOut() },
                arguments = listOf(
                    navArgument(MovieDetailsScreen.MovieIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                MovieDetailsScreen(
                    goToMoviePlayer = { movieId ->
                        navController.navigate(
                            Screens.VideoPlayer.withArgs(movieId)
                        )
                    },
                    refreshScreenWithNewMovie = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        ) {
                            popUpTo(Screens.MovieDetails()) {
                                inclusive = true
                            }
                        }
                    },
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    }
                )
            }
            composable(
                route = Screens.Collection(),
                enterTransition = { screenBlackFadeIn() },
                exitTransition = { screenBlackFadeOut() },
                popEnterTransition = { screenBlackFadeIn() },
                popExitTransition = { screenBlackFadeOut() },
                arguments = listOf(
                    navArgument(CollectionScreen.SectionIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                CollectionScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    },
                    onMovieSelected = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        )
                    },
                )
            }
            composable(
                route = Screens.SignIn(),
                enterTransition = { screenBlackFadeIn() },
                exitTransition = { screenBlackFadeOut() },
                popEnterTransition = { screenBlackFadeIn() },
                popExitTransition = { screenBlackFadeOut() },
                arguments = listOf(
                    navArgument(AuthScreenRoute.MethodBundleKey) {
                        type = NavType.StringType
                        defaultValue = "qr"
                    },
                ),
            ) {
                com.google.jetstream.presentation.screens.auth.AuthScreen(
                    onSignedIn = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screens.Dashboard()) {
                                popUpTo(Screens.Dashboard()) { inclusive = true }
                            }
                        }
                    },
                    onBackPressed = {
                        if (!navController.navigateUp()) {
                            navController.navigate(Screens.Dashboard()) {
                                popUpTo(Screens.Dashboard()) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable(
                route = Screens.Dashboard(),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                DashboardScreen(
                    openCategoryMovieList = { categoryId ->
                        navController.navigate(
                            Screens.CategoryMovieList.withArgs(categoryId)
                        )
                    },
                    openCollectionScreen = { sectionId ->
                        navController.navigate(
                            Screens.Collection.withArgs(sectionId)
                        )
                    },
                    openMovieDetailsScreen = { movieId ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movieId)
                        )
                    },
                    openVideoPlayer = { movie ->
                        navController.navigate(
                            Screens.VideoPlayer.withArgs(movie.id)
                        )
                    },
                    openVideoPlayerById = { movieId ->
                        navController.navigate(
                            Screens.VideoPlayer.withArgs(movieId)
                        )
                    },
                    openSignInPhone = {
                        navController.navigate(Screens.SignIn.withArgs("phone"))
                    },
                    openSignInEmail = {
                        navController.navigate(Screens.SignIn.withArgs("email"))
                    },
                    onBackPressed = onBackPressed,
                    isComingBackFromDifferentScreen = isComingBackFromDifferentScreen,
                    resetIsComingBackFromDifferentScreen = {
                        isComingBackFromDifferentScreen = false
                    }
                )
            }
            composable(
                route = Screens.VideoPlayer(),
                enterTransition = { screenBlackFadeIn() },
                exitTransition = { screenBlackFadeOut() },
                popEnterTransition = { screenBlackFadeIn() },
                popExitTransition = { screenBlackFadeOut() },
                arguments = listOf(
                    navArgument(VideoPlayerScreen.MovieIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                VideoPlayerScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    },
                    onPlayAnotherMovie = { movieId ->
                        navController.navigate(Screens.VideoPlayer.withArgs(movieId)) {
                            popUpTo(Screens.VideoPlayer()) { inclusive = true }
                        }
                    },
                )
            }
        },
        )
    }
}
