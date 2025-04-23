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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.messages.list.MessagesLazyListState
import io.getstream.chat.android.compose.ui.util.rememberMessagesLazyListState
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ReactionSortingByCount
import io.getstream.chat.android.previewdata.PreviewChannelUserRead
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

internal class MessageListTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `empty messages`() {
        snapshotWithDarkMode {
            MessageList(
                modifier = Modifier.fillMaxSize(),
                currentState = MessageListState(),
                reactionSorting = ReactionSortingByCount,
            )
        }
    }

    @Test
    fun `loading messages`() {
        snapshotWithDarkMode {
            MessageList(
                modifier = Modifier.fillMaxSize(),
                currentState = MessageListState(
                    isLoading = true,
                ),
                reactionSorting = ReactionSortingByCount,
            )
        }
    }

    @Test
    fun `loaded top aligned messages`() {
        snapshotWithDarkMode {
            MessageList(
                messageListState = TwoMessagesListState,
                verticalArrangement = Arrangement.Top,
            )
        }
    }

    @Test
    fun `loaded bottom aligned messages`() {
        snapshotWithDarkMode {
            MessageList(
                messageListState = TwoMessagesListState,
                verticalArrangement = Arrangement.Bottom,
            )
        }
    }

    @Test
    fun `loaded messages`() {
        snapshot {
            MessageList(messageListState = LoadedMessageListState)
        }
    }

    @Test
    fun `loaded messages in dark mode`() {
        snapshot(isInDarkMode = true) {
            MessageList(messageListState = LoadedMessageListState)
        }
    }

    @Test
    fun `scroll to bottom button`() {
        snapshot {
            val messagesLazyListState = rememberMessagesLazyListState()

            LaunchedEffect(Unit) {
                messagesLazyListState.lazyListState.scrollToItem(LoadedMessageListState.messageItems.size - 1)
            }

            MessageList(
                messagesLazyListState = messagesLazyListState,
                messageListState = LoadedMessageListState,
            )
        }
    }

    @Test
    fun `scroll to bottom button in dark mode`() {
        snapshot(isInDarkMode = true) {
            val messagesLazyListState = rememberMessagesLazyListState()

            LaunchedEffect(Unit) {
                messagesLazyListState.lazyListState.scrollToItem(LoadedMessageListState.messageItems.size - 1)
            }

            MessageList(
                messagesLazyListState = messagesLazyListState,
                messageListState = LoadedMessageListState,
            )
        }
    }

    @Test
    fun `loading older messages`() {
        snapshotWithDarkMode {
            MessageList(
                currentState = MessageListState(
                    isLoadingOlderMessages = true,
                    messageItems = listOf(
                        MessageItemState(
                            message = PreviewMessageData.message1,
                            ownCapabilities = ChannelCapabilities.toSet(),
                        ),
                    ),
                ),
                reactionSorting = ReactionSortingByCount,
            )
        }
    }
}

private val Date1 = Calendar.getInstance().apply { set(2022, 1, 1, 1, 1) }.time
private val Date2 = Calendar.getInstance().apply { set(2022, 1, 2, 2, 2) }.time
private val Date3 = Calendar.getInstance().apply { set(2022, 1, 3, 3, 3) }.time
private val Date4 = Calendar.getInstance().apply { set(2022, 1, 4, 4, 4) }.time

private val LoadedMessageListState = MessageListState(
    messageItems = listOf(
        MessageItemState(
            message = PreviewMessageData.message5,
            isMine = true,
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        SystemMessageItemState(
            message = PreviewMessageData.message4,
        ),
        MessageItemState(
            message = PreviewMessageData.messageWithError,
            isMine = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        MessageItemState(
            message = PreviewMessageData.messageWithPoll,
            isMine = true,
            showMessageFooter = true,
            isMessageRead = true,
            messageReadBy = listOf(
                PreviewChannelUserRead.channelUserRead1,
                PreviewChannelUserRead.channelUserRead2,
            ),
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        DateSeparatorItemState(Date4),
        MessageItemState(
            message = PreviewMessageData.messageWithMention.copy(
                user = PreviewUserData.user1,
                replyTo = PreviewMessageData.message3.copy(
                    user = PreviewUserData.user7,
                ),
            ),
            currentUser = PreviewUserData.user7,
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        MessageItemState(
            message = PreviewMessageData.messageWithOwnReaction.copy(
                user = PreviewUserData.user1,
            ),
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        MessageItemState(
            message = PreviewMessageData.message3,
            isMine = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        DateSeparatorItemState(Date3),
        MessageItemState(
            message = PreviewMessageData.message2.copy(
                user = PreviewUserData.user1,
            ),
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        DateSeparatorItemState(Date2),
        MessageItemState(
            message = PreviewMessageData.messageDeleted.copy(
                user = PreviewUserData.user1,
            ),
            deletedMessageVisibility = DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER,
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        MessageItemState(
            message = PreviewMessageData.message1,
            groupPosition = listOf(MessagePosition.TOP),
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        DateSeparatorItemState(Date1),
    ),
)

private val TwoMessagesListState = MessageListState(
    messageItems = listOf(
        MessageItemState(
            message = PreviewMessageData.message3,
            isMine = true,
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
        MessageItemState(
            message = PreviewMessageData.message5,
            isMine = false,
            showMessageFooter = true,
            ownCapabilities = ChannelCapabilities.toSet(),
        ),
    ),
)

@Composable
private fun MessageList(
    messagesLazyListState: MessagesLazyListState = rememberMessagesLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    messageListState: MessageListState,
) {
    MessageList(
        messagesLazyListState = messagesLazyListState,
        currentState = messageListState,
        reactionSorting = ReactionSortingByCount,
        verticalArrangement = verticalArrangement,
    )
}
