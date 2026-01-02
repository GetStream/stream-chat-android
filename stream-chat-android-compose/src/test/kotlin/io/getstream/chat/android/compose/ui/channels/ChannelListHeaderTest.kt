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

package io.getstream.chat.android.compose.ui.channels

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeaderConnectedNoUser
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeaderConnectedWithUser
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeaderConnectingNoUser
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeaderConnectingWithUser
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeaderOfflineNoUser
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeaderOfflineWithUser
import org.junit.Rule
import org.junit.Test

internal class ChannelListHeaderTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `connected, no user`() {
        snapshotWithDarkMode {
            ChannelListHeaderConnectedNoUser()
        }
    }

    @Test
    fun `connected, with user`() {
        snapshotWithDarkMode {
            ChannelListHeaderConnectedWithUser()
        }
    }

    @Test
    fun `connecting, no user`() {
        snapshotWithDarkMode {
            ChannelListHeaderConnectingNoUser()
        }
    }

    @Test
    fun `connecting, with user`() {
        snapshotWithDarkMode {
            ChannelListHeaderConnectingWithUser()
        }
    }

    @Test
    fun `offline, no user`() {
        snapshotWithDarkMode {
            ChannelListHeaderOfflineNoUser()
        }
    }

    @Test
    fun `offline, with user`() {
        snapshotWithDarkMode {
            ChannelListHeaderOfflineWithUser()
        }
    }
}
