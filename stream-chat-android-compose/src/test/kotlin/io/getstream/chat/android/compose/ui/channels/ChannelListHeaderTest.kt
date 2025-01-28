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

package io.getstream.chat.android.compose.ui.channels

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewUserData
import org.junit.Rule
import org.junit.Test

internal class ChannelListHeaderTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `titled, connected, no user`() {
        snapshotWithDarkMode {
            ChannelListHeader(
                title = "title",
                connectionState = ConnectionState.Connected,
            )
        }
    }

    @Test
    fun `titled, connected, with user`() {
        snapshotWithDarkMode {
            ChannelListHeader(
                title = "title",
                currentUser = PreviewUserData.user1,
                connectionState = ConnectionState.Connected,
            )
        }
    }

    @Test
    fun `untitled, connected, no user`() {
        snapshotWithDarkMode {
            ChannelListHeader(
                connectionState = ConnectionState.Connected,
            )
        }
    }

    @Test
    fun `untitled, offline, no user`() {
        snapshotWithDarkMode {
            ChannelListHeader(
                connectionState = ConnectionState.Offline,
            )
        }
    }

    @Test
    fun `untitled, connecting, no user`() {
        snapshotWithDarkMode {
            ChannelListHeader(
                connectionState = ConnectionState.Connecting,
            )
        }
    }
}
