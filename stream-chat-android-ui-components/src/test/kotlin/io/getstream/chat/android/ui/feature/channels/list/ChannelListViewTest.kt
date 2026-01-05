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

package io.getstream.chat.android.ui.feature.channels.list

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.PaparazziViewTest
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.helper.CurrentUserProvider
import org.junit.Before
import org.junit.Test

internal class ChannelListViewTest : PaparazziViewTest() {

    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_4A)

    @Before
    fun setUp() {
        ChatUI.currentUserProvider = CurrentUserProvider { PreviewUserData.user1 }
    }

    @Test
    fun `empty channels`() {
        channelList {
            setChannels(emptyList())
        }
    }

    @Test
    fun `loading channels`() {
        channelList {
            showLoadingView()
        }
    }

    @Test
    fun `loaded channels`() {
        channelList {
            setChannels(
                listOf(
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithImage,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithOneUser,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithOnlineUser,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithFewMembers,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithManyMembers,
                        typingUsers = listOf(PreviewUserData.user2),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithMessages.copy(
                            members = listOf(
                                Member(user = PreviewUserData.user1),
                                Member(user = PreviewUserData.user2),
                                Member(user = PreviewUserData.user3),
                                Member(user = PreviewUserData.user4),
                            ),
                            extraData = mapOf("mutedChannel" to true),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                ),
            )
        }
    }

    @Test
    fun `loading more channels`() {
        channelList {
            setChannels(
                listOf(
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithImage,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithOneUser,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithOnlineUser,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithFewMembers,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithManyMembers,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = PreviewChannelData.channelWithMessages,
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.LoadingMoreItem,
                ),
            )
        }
    }

    private fun channelList(block: ChannelListView.() -> Unit) {
        snapshotColumn { context -> ChannelListView(context).apply { block() } }
    }
}
