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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Holds the state of the messages list screen.
 *
 * @param messageItems The list of [MessageListItemState]s to be shown in the list.
 * @param endOfNewMessagesReached Whether the user has reached the newest message or not.
 * @param endOfOldMessagesReached Whether the user has reached the older message or not.
 * @param isLoading Whether the initial loading is in progress or not.
 * @param isLoadingNewerMessages Whether loading of a page with newer messages is in progress or not.
 * @param isLoadingOlderMessages Whether loading of a page with older messages is in progress or not.
 * @param currentUser The current logged in [User].
 * @param parentMessageId The [Message] id if we are in a thread, null otherwise.
 * @param unreadCount Count of unread messages in channel or thread.
 * @param newMessageState The [NewMessageState] of the newly received message.
 * @param selectedMessageState The current [SelectedMessageState].
 */
public data class MessageListState(
    public val messageItems: List<MessageListItemState> = emptyList(),
    public val endOfNewMessagesReached: Boolean = true,
    public val endOfOldMessagesReached: Boolean = false,
    public val isLoading: Boolean = false,
    public val isLoadingNewerMessages: Boolean = false,
    public val isLoadingOlderMessages: Boolean = false,
    public val currentUser: User? = User(),
    public val parentMessageId: String? = null,
    public val unreadCount: Int = 0,
    public val newMessageState: NewMessageState? = null,
    public val selectedMessageState: SelectedMessageState? = null,
)

internal fun MessageListState.stringify(): String {
    return "MessageListState(" +
        "messageItems.size: ${messageItems.size}, " +
        "newMessageState: $newMessageState, " +
        "endOfNewMessagesReached: $endOfNewMessagesReached, " +
        "endOfOldMessagesReached: $endOfOldMessagesReached, " +
        "isLoading: $isLoading, " +
        "isLoadingNewerMessages: $isLoadingNewerMessages, " +
        "isLoadingOlderMessages: $isLoadingOlderMessages, " +
        "currentUser.id: ${currentUser?.id}, " +
        "parentMessageId: $parentMessageId, " +
        "unreadCount: $unreadCount, " +
        "selectedMessageState: $selectedMessageState)"
}

internal inline fun <reified T : MessageListItemState> MessageListState.lastItemOrNull(): T? {
    return messageItems.lastOrNull { it is T } as T?
}
