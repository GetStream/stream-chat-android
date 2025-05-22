/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
internal class ChannelInfoMemberInfoModalSheetTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `banned member`() {
        snapshot {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            LaunchedEffect(Unit) {
                sheetState.show()
            }
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = ChatTheme.colors.barsBackground,
                onDismissRequest = {},
            ) {
                ChannelInfoMemberInfoModalSheetContent(banned = true)
            }
        }
    }

    @Test
    fun `banned member in dark mode`() {
        snapshot(isInDarkMode = true) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            LaunchedEffect(Unit) {
                sheetState.show()
            }
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = ChatTheme.colors.barsBackground,
                onDismissRequest = {},
            ) {
                ChannelInfoMemberInfoModalSheetContent(banned = true)
            }
        }
    }

    @Test
    fun `not banned member`() {
        snapshot {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            LaunchedEffect(Unit) {
                sheetState.show()
            }
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = ChatTheme.colors.barsBackground,
                onDismissRequest = {},
            ) {
                ChannelInfoMemberInfoModalSheetContent(banned = false)
            }
        }
    }

    @Test
    fun `not banned member in dark mode`() {
        snapshot(isInDarkMode = true) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            LaunchedEffect(Unit) {
                sheetState.show()
            }
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = ChatTheme.colors.barsBackground,
                onDismissRequest = {},
            ) {
                ChannelInfoMemberInfoModalSheetContent(banned = false)
            }
        }
    }
}
