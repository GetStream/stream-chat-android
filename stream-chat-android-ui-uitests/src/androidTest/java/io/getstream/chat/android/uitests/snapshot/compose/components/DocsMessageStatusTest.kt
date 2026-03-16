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

package io.getstream.chat.android.uitests.snapshot.compose.components

import io.getstream.chat.android.compose.ui.messages.list.MessageContainer
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.ReactionSortingByCount
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class DocsMessageStatusTest : ComposeScreenshotTest() {

    private fun statusMessage(text: String = "Hey there!") = TestData.message1().copy(
        text = text,
        user = TestData.alex(),
    )

    @Test
    fun statusPending() = runScreenshotTest {
        MessageContainer(
            messageItem = MessageItemState(
                message = statusMessage().copy(syncStatus = SyncStatus.IN_PROGRESS),
                isMine = true,
                showMessageFooter = true,
                ownCapabilities = emptySet(),
            ),
            reactionSorting = ReactionSortingByCount,
            onLongItemClick = {},
        )
    }

    @Test
    fun statusSent() = runScreenshotTest {
        MessageContainer(
            messageItem = MessageItemState(
                message = statusMessage().copy(syncStatus = SyncStatus.COMPLETED),
                isMine = true,
                isMessageRead = false,
                isMessageDelivered = false,
                showMessageFooter = true,
                ownCapabilities = emptySet(),
            ),
            reactionSorting = ReactionSortingByCount,
            onLongItemClick = {},
        )
    }

    @Test
    fun statusDelivered() = runScreenshotTest {
        MessageContainer(
            messageItem = MessageItemState(
                message = statusMessage().copy(syncStatus = SyncStatus.COMPLETED),
                isMine = true,
                isMessageRead = false,
                isMessageDelivered = true,
                showMessageFooter = true,
                ownCapabilities = emptySet(),
            ),
            reactionSorting = ReactionSortingByCount,
            onLongItemClick = {},
        )
    }

    @Test
    fun statusRead() = runScreenshotTest {
        MessageContainer(
            messageItem = MessageItemState(
                message = statusMessage().copy(syncStatus = SyncStatus.COMPLETED),
                isMine = true,
                isMessageRead = true,
                showMessageFooter = true,
                ownCapabilities = emptySet(),
            ),
            reactionSorting = ReactionSortingByCount,
            onLongItemClick = {},
        )
    }

    @Test
    fun statusReadGroup() = runScreenshotTest {
        MessageContainer(
            messageItem = MessageItemState(
                message = statusMessage().copy(syncStatus = SyncStatus.COMPLETED),
                isMine = true,
                isMessageRead = true,
                showMessageFooter = true,
                messageReadBy = listOf(
                    ChannelUserRead(
                        user = TestData.elena(),
                        lastReceivedEventDate = TestData.date1(),
                        unreadMessages = 0,
                        lastRead = TestData.date1(),
                        lastReadMessageId = "message1",
                    ),
                    ChannelUserRead(
                        user = TestData.sarah(),
                        lastReceivedEventDate = TestData.date1(),
                        unreadMessages = 0,
                        lastRead = TestData.date1(),
                        lastReadMessageId = "message1",
                    ),
                ),
                ownCapabilities = emptySet(),
            ),
            reactionSorting = ReactionSortingByCount,
            onLongItemClick = {},
        )
    }
}
