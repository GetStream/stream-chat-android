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
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemDraftMessage
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemLastMessageDeliveredStatus
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemLastMessagePendingStatus
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemLastMessageSeenStatus
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemLastMessageSentStatus
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemMuted
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemMutedTrailingBottom
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemNoMessages
import io.getstream.chat.android.compose.ui.channels.list.ChannelItemUnreadMessages
import io.getstream.chat.android.compose.ui.theme.ChannelListConfig
import io.getstream.chat.android.compose.ui.theme.ChatConfig
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MuteIndicatorPosition
import org.junit.Rule
import org.junit.Test

internal class ChannelItemTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `no messages`() {
        snapshotWithDarkMode {
            ChannelItemNoMessages()
        }
    }

    @Test
    fun `muted channel`() {
        snapshotWithDarkMode {
            ChannelItemMuted()
        }
    }

    @Test
    fun `muted channel trailing bottom`() {
        snapshotWithDarkMode {
            ChatTheme(
                config = ChatConfig(
                    channelList = ChannelListConfig(
                        muteIndicatorPosition = MuteIndicatorPosition.TrailingBottom,
                    ),
                ),
            ) {
                ChannelItemMutedTrailingBottom()
            }
        }
    }

    @Test
    fun `unread messages`() {
        snapshotWithDarkMode {
            ChannelItemUnreadMessages()
        }
    }

    @Test
    fun `last message pending status`() {
        snapshotWithDarkMode {
            ChannelItemLastMessagePendingStatus()
        }
    }

    @Test
    fun `last message sent status`() {
        snapshotWithDarkMode {
            ChannelItemLastMessageSentStatus()
        }
    }

    @Test
    fun `last message delivered status`() {
        snapshotWithDarkMode {
            ChannelItemLastMessageDeliveredStatus()
        }
    }

    @Test
    fun `last message seen status`() {
        snapshotWithDarkMode {
            ChannelItemLastMessageSeenStatus()
        }
    }

    @Test
    fun `draft message`() {
        snapshotWithDarkMode {
            ChannelItemDraftMessage()
        }
    }
}
