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

package io.getstream.chat.android.common.messagelist

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.model.messsagelist.MessageListItem
import io.getstream.chat.android.common.state.messagelist.NewMessageState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageState

/**
 * Holds the state of the messages list screen.
 *
 * @param messages The list of [MessageListItem]s to be shown in the list.
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
    public val messages: List<MessageListItem> = emptyList(),
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