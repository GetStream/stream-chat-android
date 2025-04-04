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

package io.getstream.chat.android.uitests.snapshot.uicomponents.channels

import android.content.Context
import android.widget.FrameLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.images.internal.StreamCoil
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItemViewType
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.helper.CurrentUserProvider
import io.getstream.chat.android.uitests.util.FakeImageLoader
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Before
import org.junit.Test

class ChannelListItemViewTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(InternalStreamChatApi::class)
    @Before
    fun setup() {
        StreamCoil.setImageLoader { FakeImageLoader(context) }
        ChatUI.currentUserProvider = CurrentUserProvider { TestData.user1() }
    }

    @Test
    fun channelItemWithUnreadCount() {
        renderChannelListItemView(
            TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                ),
                messages = listOf(
                    TestData.message1(),
                    TestData.message2(),
                ),
                unreadCount = 2,
                channelLastMessageAt = TestData.date2(),
            ),
        )
    }

    @Test
    fun channelItemForMutedChannel() {
        renderChannelListItemView(
            TestData.channel1().let {
                it.copy(
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                    ),
                    messages = listOf(
                        TestData.message1(),
                        TestData.message2(),
                    ),
                    channelLastMessageAt = TestData.date2(),
                    extraData = it.extraData + mapOf("mutedChannel" to true),
                    // extraData = extraData["mutedChannel"] = true,
                )
            },
        )
    }

    @Test
    fun channelItemForChannelWithoutMessages() {
        renderChannelListItemView(
            TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                ),
            ),
        )
    }

    private fun renderChannelListItemView(channel: Channel) {
        val viewHolder = ChannelListItemViewHolderFactory()
            .createViewHolder(FrameLayout(context), ChannelListItemViewType.DEFAULT)

        val channelListItem = ChannelListItem.ChannelItem(channel, emptyList(), draftMessage = null)
        viewHolder.bind(channelListItem, CHANNEL_LIST_PAYLOAD_DIFF)

        compareScreenshot(viewHolder.itemView)
    }

    companion object {
        private val CHANNEL_LIST_PAYLOAD_DIFF: ChannelListPayloadDiff = ChannelListPayloadDiff(
            nameChanged = true,
            avatarViewChanged = true,
            usersChanged = true,
            lastMessageChanged = true,
            readStateChanged = true,
            unreadCountChanged = true,
            extraDataChanged = true,
            typingUsersChanged = true,
            draftMessageChanged = true,
        )
    }
}
