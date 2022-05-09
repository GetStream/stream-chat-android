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

import androidx.compose.ui.test.junit4.createComposeRule
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.channels.list.ChannelItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.uitests.snapshot.compose.TestChatTheme
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Rule
import org.junit.Test

class ChannelItemTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun channelItemWithUnreadCount() {
        renderChannelItem(
            channelItemState = ChannelItemState(
                channel = TestData.channel1().apply {
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    )
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2()
                    )
                    unreadCount = 2
                    lastMessageAt = TestData.date2()
                }
            ),
            currentUser = TestData.user1()
        )
    }

    @Test
    fun channelItemWithoutCurrentUser() {
        renderChannelItem(
            channelItemState = ChannelItemState(
                channel = TestData.channel1().apply {
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    )
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2()
                    )
                    lastMessageAt = TestData.date2()
                }
            ),
            currentUser = null
        )
    }

    @Test
    fun channelItemForMutedChannel() {
        renderChannelItem(
            channelItemState = ChannelItemState(
                channel = TestData.channel1().apply {
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    )
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2()
                    )
                    lastMessageAt = TestData.date2()
                },
                isMuted = true
            ),
            currentUser = TestData.user1()
        )
    }

    @Test
    fun channelItemForChannelWithoutMessages() {
        renderChannelItem(
            channelItemState = ChannelItemState(
                channel = TestData.channel1().apply {
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    )
                },
            ),
            currentUser = TestData.user1()
        )
    }

    private fun renderChannelItem(
        channelItemState: ChannelItemState,
        currentUser: User?,
    ) {
        composeRule.setContent {
            TestChatTheme {
                ChannelItem(
                    channelItem = channelItemState,
                    currentUser = currentUser,
                    onChannelClick = {},
                    onChannelLongClick = {}
                )
            }
        }
        compareScreenshot(composeRule)
    }
}
