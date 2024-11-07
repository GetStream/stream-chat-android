/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.snapshot.compose.channels

import io.getstream.chat.android.compose.state.channels.list.ItemState.ChannelItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class ChannelItemTest : ComposeScreenshotTest() {

    @Test
    fun channelItemWithUnreadCount() = runScreenshotTest {
        ChannelItem(
            channelItem = ChannelItemState(
                channel = TestData.channel1().copy(
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    ),
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2(),
                    ),
                    unreadCount = 2,
                    lastMessageAt = TestData.date2(),
                ),
                typingUsers = emptyList(),
            ),
            currentUser = TestData.user1(),
            onChannelClick = {},
            onChannelLongClick = {},
        )
    }

    @Test
    fun channelItemWithoutCurrentUser() = runScreenshotTest {
        ChannelItem(
            channelItem = ChannelItemState(
                channel = TestData.channel1().copy(
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    ),
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2(),
                    ),
                    lastMessageAt = TestData.date2(),
                ),
                typingUsers = emptyList(),
            ),
            currentUser = null,
            onChannelClick = {},
            onChannelLongClick = {},
        )
    }

    @Test
    fun channelItemForMutedChannel() = runScreenshotTest {
        ChannelItem(
            channelItem = ChannelItemState(
                channel = TestData.channel1().copy(
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    ),
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2(),
                    ),
                    lastMessageAt = TestData.date2(),
                ),
                isMuted = true,
                typingUsers = emptyList(),
            ),
            currentUser = TestData.user1(),
            onChannelClick = {},
            onChannelLongClick = {},
        )
    }

    @Test
    fun channelItemForChannelWithoutMessages() = runScreenshotTest {
        ChannelItem(
            channelItem = ChannelItemState(
                channel = TestData.channel1().copy(
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    ),
                ),
                typingUsers = emptyList(),
            ),
            currentUser = TestData.user1(),
            onChannelClick = {},
            onChannelLongClick = {},
        )
    }
}
