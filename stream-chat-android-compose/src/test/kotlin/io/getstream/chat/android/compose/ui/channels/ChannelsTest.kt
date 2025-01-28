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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.Channels
import io.getstream.chat.android.compose.ui.channels.list.SearchResultItem
import io.getstream.chat.android.compose.ui.channels.list.WrapperItemContent
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import org.junit.Rule
import org.junit.Test

internal class ChannelsTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `loaded channels`() {
        snapshot {
            Channels(
                modifier = Modifier.fillMaxSize(),
                channelsState = ChannelsState(
                    isLoading = false,
                    channelItems = listOf(
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithImage),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOneUser),
                        ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOnlineUser),
                    ),
                ),
                lazyListState = rememberLazyListState(),
                itemContent = { itemState ->
                    WrapperItemContent(
                        itemState = itemState,
                        channelContent = { channelItem ->
                            ChannelItem(
                                channelItem = channelItem,
                                currentUser = PreviewUserData.user1,
                                onChannelClick = {},
                                onChannelLongClick = {},
                            )
                        },
                        searchResultContent = { searchResultItem ->
                            SearchResultItem(
                                searchResultItemState = searchResultItem,
                                currentUser = PreviewUserData.user1,
                                onSearchResultClick = {},
                            )
                        },
                    )
                },
                onLastItemReached = {},
            )
        }
    }
}
