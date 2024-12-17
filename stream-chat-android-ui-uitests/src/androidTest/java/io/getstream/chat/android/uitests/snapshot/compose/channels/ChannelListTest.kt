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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState.ChannelItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class ChannelListTest : ComposeScreenshotTest() {

    @Test
    fun channelListWithContentState() = runScreenshotTest {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = ChannelsState(
                isLoading = false,
                isLoadingMore = false,
                channelItems = listOf(
                    ChannelItemState(
                        channel = TestData.channel1().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member2(),
                            ),
                            messages = listOf(
                                TestData.message1(),
                            ),
                            channelLastMessageAt = TestData.date1(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel2().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member3(),
                            ),
                            messages = listOf(
                                TestData.message2(),
                            ),
                            channelLastMessageAt = TestData.date2(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel3().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member4(),
                            ),
                            messages = listOf(
                                TestData.message3(),
                            ),
                            channelLastMessageAt = TestData.date3(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel4().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member5(),
                            ),
                            messages = listOf(
                                TestData.message4(),
                            ),
                            channelLastMessageAt = TestData.date4(),
                        ),
                        typingUsers = emptyList(),
                    ),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelListWithEmptyState() = runScreenshotTest {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = ChannelsState(
                isLoading = false,
                isLoadingMore = false,
                channelItems = emptyList(),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelListWithLoadingMoreState() = runScreenshotTest {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = ChannelsState(
                isLoading = false,
                isLoadingMore = true,
                channelItems = listOf(
                    ChannelItemState(
                        channel = TestData.channel1().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member2(),
                            ),
                            messages = listOf(
                                TestData.message1(),
                            ),
                            channelLastMessageAt = TestData.date1(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel2().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member3(),
                            ),
                            messages = listOf(
                                TestData.message2(),
                            ),
                            channelLastMessageAt = TestData.date2(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel3().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member4(),
                            ),
                            messages = listOf(
                                TestData.message3(),
                            ),
                            channelLastMessageAt = TestData.date3(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel4().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member5(),
                            ),
                            messages = listOf(
                                TestData.message4(),
                            ),
                            channelLastMessageAt = TestData.date4(),
                        ),
                        typingUsers = emptyList(),
                    ),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelListWithLoadingState() = runScreenshotTest {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = ChannelsState(
                isLoading = true,
                channelItems = emptyList(),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelListWithContentAndLoadingState() = runScreenshotTest {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = ChannelsState(
                isLoading = true,
                isLoadingMore = false,
                channelItems = listOf(
                    ChannelItemState(
                        channel = TestData.channel1().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member2(),
                            ),
                            messages = listOf(
                                TestData.message1(),
                            ),
                            channelLastMessageAt = TestData.date1(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel2().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member3(),
                            ),
                            messages = listOf(
                                TestData.message2(),
                            ),
                            channelLastMessageAt = TestData.date2(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel3().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member4(),
                            ),
                            messages = listOf(
                                TestData.message3(),
                            ),
                            channelLastMessageAt = TestData.date3(),
                        ),
                        typingUsers = emptyList(),
                    ),
                    ChannelItemState(
                        channel = TestData.channel4().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member5(),
                            ),
                            messages = listOf(
                                TestData.message4(),
                            ),
                            channelLastMessageAt = TestData.date4(),
                        ),
                        typingUsers = emptyList(),
                    ),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }
}
