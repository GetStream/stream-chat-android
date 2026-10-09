/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.ui

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.app.ActivityOptionsCompat
import app.cash.paparazzi.Paparazzi

/**
 * Base for Paparazzi snapshot tests of the AI compose components.
 *
 * Implementers expose a [Paparazzi] rule and call [snapshot] to render a component inside the
 * shared test environment: [MaterialTheme] (the module has no `ChatTheme`), with
 * [LocalInspectionMode] enabled and a no-op [ActivityResultRegistryOwner] so components that use
 * `rememberLauncherForActivityResult` (such as the composer) render without a host activity.
 */
internal interface PaparazziTest {

    val paparazzi: Paparazzi

    fun snapshot(
        name: String? = null,
        composable: @Composable () -> Unit,
    ) {
        paparazzi.snapshot(name) {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
                LocalActivityResultRegistryOwner provides NoOpResultRegistryOwner,
            ) {
                Column {
                    // light theme
                    MaterialTheme(
                        colorScheme = lightColorScheme(),
                    ) {
                        Surface {
                            composable()
                        }
                    }
                    // dark theme
                    MaterialTheme(
                        colorScheme = darkColorScheme(),
                    ) {
                        Surface {
                            composable()
                        }
                    }
                }
            }
        }
    }
}

private val NoOpResultRegistryOwner = object : ActivityResultRegistryOwner {
    override val activityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            // No-op: snapshots never launch activities.
        }
    }
}
