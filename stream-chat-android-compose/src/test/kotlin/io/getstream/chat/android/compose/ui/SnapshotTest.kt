/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import org.junit.Rule

internal interface SnapshotTest : ComposeTest {

    @get:Rule
    val paparazzi: Paparazzi

    fun snapshot(
        isInDarkMode: Boolean = false,
        composable: @Composable () -> Unit,
    ) {
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
            ) {
                ChatTheme(isInDarkMode = isInDarkMode) {
                    Box(modifier = Modifier.background(ChatTheme.colors.appBackground)) {
                        composable.invoke()
                    }
                }
            }
        }
    }

    fun snapshotWithDarkMode(composable: @Composable () -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
            ) {
                Column {
                    ChatTheme(isInDarkMode = true) {
                        Box(
                            modifier = Modifier
                                .weight(.5f)
                                .background(ChatTheme.colors.appBackground),
                        ) {
                            composable.invoke()
                        }
                    }
                    ChatTheme(isInDarkMode = false) {
                        Box(
                            modifier = Modifier
                                .weight(.5f)
                                .background(ChatTheme.colors.appBackground),
                        ) {
                            composable.invoke()
                        }
                    }
                }
            }
        }
    }

    fun snapshotWithDarkModeRow(composable: @Composable () -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
            ) {
                Row {
                    ChatTheme(isInDarkMode = true) {
                        Box(
                            modifier = Modifier
                                .weight(.5f)
                                .background(ChatTheme.colors.appBackground),
                        ) {
                            composable.invoke()
                        }
                    }
                    ChatTheme(isInDarkMode = false) {
                        Box(
                            modifier = Modifier
                                .weight(.5f)
                                .background(ChatTheme.colors.appBackground),
                        ) {
                            composable.invoke()
                        }
                    }
                }
            }
        }
    }
}
