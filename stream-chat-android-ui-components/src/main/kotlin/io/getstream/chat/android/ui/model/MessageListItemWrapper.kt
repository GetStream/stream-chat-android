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

package io.getstream.chat.android.ui.model

import io.getstream.chat.android.ui.common.state.messages.list.NewMessageState
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * MessageListItemWrapper wraps a list of [MessageListItem] with a few extra fields.
 *
 * @param items The list of [MessageListItem]s to be shown in the list.
 * @param hasNewMessages Whether the user has new messages or not.
 * @param isTyping Whether the user is typing or not.
 * @param isThread Whether the user is in a thread or not.
 * @param areNewestMessagesLoaded Whether the newest messages are loaded or not.
 */
public data class MessageListItemWrapper(
    val items: List<MessageListItem> = listOf(),
    val hasNewMessages: Boolean = false,
    val isTyping: Boolean = false,
    val isThread: Boolean = false,
    @Deprecated(
        "The name of this field will to be aligned with `MessageListState.endOfNewMessagesReached` field.",
    )
    val areNewestMessagesLoaded: Boolean = true,
    internal val newMessageState: NewMessageState? = null,
) {

    override fun toString(): String {
        return stringify()
    }

    private fun stringify(): String {
        return "MessageListItemWrapper(" +
            "endOfNewMessagesReached=$areNewestMessagesLoaded" +
            ", hasNewMessages=$hasNewMessages" +
            ", newMessageState=$newMessageState" +
            ", items=${items.size}" +
            ", first: ${items.firstOrNull()?.stringify()}" +
            ", isTyping=$isTyping" +
            ", isThread=$isThread" +
            ")"
    }
}
