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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import org.junit.Rule
import org.junit.Test

internal class ChannelListTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_4A)

    @Test
    fun `empty channels`() {
        snapshotWithDarkMode {
            ChannelList(
                modifier = Modifier.fillMaxSize(),
                currentUser = PreviewUserData.user1,
                channelsState = ChannelsState(isLoading = false),
            )
        }
    }

    @Test
    fun `loading channels`() {
        snapshotWithDarkMode {
            ChannelList(
                modifier = Modifier.fillMaxSize(),
                currentUser = PreviewUserData.user1,
                channelsState = ChannelsState(isLoading = true),
            )
        }
    }

    @Test
    fun `loaded channels`() {
        snapshotWithDarkMode {
            ChannelList(
                modifier = Modifier.fillMaxSize(),
                currentUser = PreviewUserData.user1,
                channelsState = ChannelsState(
                    isLoading = false,
                    channelItems = listOf(
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithImage),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOneUser),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOnlineUser),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithFewMembers),
                        ItemState.ChannelItemState(
                            channel = PreviewChannelData.channelWithManyMembers,
                            typingUsers = listOf(PreviewUserData.user2),
                        ),
                        ItemState.ChannelItemState(
                            channel = PreviewChannelData.channelWithMessages.copy(
                                members = listOf(
                                    Member(user = PreviewUserData.user1),
                                    Member(user = PreviewUserData.user2),
                                    Member(user = PreviewUserData.user3),
                                    Member(user = PreviewUserData.user4),
                                ),
                            ),
                            isMuted = true,
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `loading more channels`() {
        snapshotWithDarkMode {
            ChannelList(
                modifier = Modifier.fillMaxSize(),
                currentUser = PreviewUserData.user1,
                channelsState = ChannelsState(
                    isLoading = false,
                    isLoadingMore = true,
                    channelItems = listOf(
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithImage),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOneUser),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOnlineUser),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithFewMembers),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithManyMembers),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithMessages),
                    ),
                ),
            )
        }
    }

    @Test
    fun `search results`() {
        snapshotWithDarkMode {
            ChannelList(
                modifier = Modifier.fillMaxSize(),
                currentUser = PreviewUserData.user1,
                channelsState = ChannelsState(
                    isLoading = false,
                    channelItems = listOf(
                        ItemState.SearchResultItemState(
                            message = PreviewMessageData.message1,
                            channel = PreviewChannelData.channelWithImage,
                        ),
                        ItemState.SearchResultItemState(message = PreviewMessageData.message2),
                    ),
                ),
            )
        }
    }

    @Test
    fun `empty search results`() {
        snapshotWithDarkMode {
            ChannelList(
                modifier = Modifier.fillMaxSize(),
                currentUser = PreviewUserData.user1,
                channelsState = ChannelsState(
                    isLoading = false,
                    searchQuery = SearchQuery.Channels("query"),
                ),
            )
        }
    }
}
