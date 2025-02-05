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

package io.getstream.chat.android.compose.ui.messages

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import org.junit.Rule
import org.junit.Test

internal class MessageListHeaderTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `connected state`() {
        snapshotWithDarkMode {
            MessageListHeader(
                channel = PreviewChannelData.channelWithFewMembers,
                currentUser = PreviewUserData.user1,
                connectionState = ConnectionState.Connected,
            )
        }
    }

    @Test
    fun `offline state`() {
        snapshotWithDarkMode {
            MessageListHeader(
                channel = PreviewChannelData.channelWithFewMembers,
                currentUser = PreviewUserData.user1,
                connectionState = ConnectionState.Offline,
            )
        }
    }

    @Test
    fun `thread mode`() {
        snapshotWithDarkMode {
            MessageListHeader(
                channel = PreviewChannelData.channelWithFewMembers,
                currentUser = PreviewUserData.user1,
                connectionState = ConnectionState.Connected,
                messageMode = MessageMode.MessageThread(
                    parentMessage = PreviewMessageData.message1,
                    threadState = null,
                ),
            )
        }
    }
}
