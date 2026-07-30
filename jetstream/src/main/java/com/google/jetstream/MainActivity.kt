/*
 * Copyright 2024 Google LLC
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

package com.google.jetstream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.presentation.AppSplashGate
import com.google.jetstream.presentation.App
import com.google.jetstream.presentation.theme.JetStreamTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var movieRepository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { AppSplashGate.keepSplashScreen }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            runCatching { movieRepository.warmHomeCache() }
        }

        setContent {
            JetStreamTheme {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    App(
                        onBackPressed = onBackPressedDispatcher::onBackPressed,
                    )
                }
            }
        }
    }
}
