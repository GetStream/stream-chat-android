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

package io.getstream.chat.android.ui.model

import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * MessageListItemWrapper wraps a list of [MessageListItem] with a few extra fields.
 */
public data class MessageListItemWrapper(
    val items: List<MessageListItem> = listOf(),
    val hasNewMessages: Boolean = false,
    val isTyping: Boolean = false,
    val isThread: Boolean = false,
    val areNewestMessagesLoaded: Boolean = true,
) {

    override fun toString(): String {
        return stringify()
    }

    public fun stringify(): String {
        return "MessageListItemWrapper(items=${items.size}, first: ${items.firstOrNull()?.stringify()}" +
            "hasNewMessages=$hasNewMessages, isTyping=$isTyping, isThread=$isThread, areNewestMessagesLoaded=$areNewestMessagesLoaded)"
    }

}
