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

import androidx.compose.ui.unit.IntSize
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    /**
     * Notifies the UI of the calculated message offset to center it on the screen.
     */
    private val _focusedMessageOffset: MutableStateFlow<Int?> = MutableStateFlow(null)
    val focusedMessageOffset: StateFlow<Int?> = _focusedMessageOffset

    /**
     * Calculates the message offset needed for the message to center inside the list on scroll.
     *
     * @param parentSize The size of the list which contains the message.
     * @param focusedMessageSize The size of the message item we wish to bring to the center and focus.
     */
    public fun calculateMessageOffset(parentSize: IntSize, focusedMessageSize: IntSize) {
        val sizeDiff = parentSize.height - focusedMessageSize.height
        if (sizeDiff > 0) {
            _focusedMessageOffset.value = -sizeDiff / 2
        } else {
            _focusedMessageOffset.value = -sizeDiff
        }
    }
}
