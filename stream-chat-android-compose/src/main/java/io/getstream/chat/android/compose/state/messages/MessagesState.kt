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

package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState

/**
 * UI representation of the Conversation/Messages screen. Holds all the data required to show messages.
 *
 * @param isLoading If we're loading (initial load).
 * @param isLoadingMore If we're loading more data (pagination).
 * @param endOfMessages If we're at the end of messages (to stop pagination).
 * @param messageItems Message items to represent in the list.
 * @param selectedMessageState The state that represents the currently selected message or message reactions.
 * @param currentUser The data of the current user, required for various UI states.
 * @param newMessageState The state that represents any new messages.
 * @param parentMessageId The id of the parent message - if we're in a thread.
 * @param unreadCount The count of messages we haven't read yet.
 */
public data class MessagesState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endOfMessages: Boolean = false,
    val messageItems: List<MessageListItemState> = emptyList(),
    val selectedMessageState: SelectedMessageState? = null,
    val currentUser: User? = null,
    val newMessageState: NewMessageState? = null,
    val parentMessageId: String? = null,
    val unreadCount: Int = 0,
) {

    internal fun isGroupedWithNextMessage(message: MessageItemState): Boolean {
        if (message.groupPosition == MessageItemGroupPosition.Bottom) {
            return false
        }
        val messageIndex = messageItems.indexOf(message)
        val nextMessage = messageItems.take(messageIndex).findLast { it is MessageItemState } as? MessageItemState ?: return false
        return (nextMessage.message.createdAt?.time ?: 0) - (message.message.createdAt?.time ?: 0) < 1000 * 60
    }
}
