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

package io.getstream.chat.android.ui.feature.messages.list.adapter

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem.DateSeparatorItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem.LoadingMoreIndicatorItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem.MessageItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem.ThreadSeparatorItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem.TypingItem
import java.util.Date

/**
 * [MessageListItem] represents elements that are displayed in a [MessageListView].
 * There are the following subclasses of the [MessageListItem] available:
 * - [DateSeparatorItem]
 * - [MessageItem]
 * - [TypingItem]
 * - [ThreadSeparatorItem]
 * - [LoadingMoreIndicatorItem]
 */
public sealed class MessageListItem {

    public fun getStableId(): Long {
        return when (this) {
            is TypingItem -> TYPING_ITEM_STABLE_ID
            is ThreadSeparatorItem -> THREAD_SEPARATOR_ITEM_STABLE_ID
            is MessageItem -> message.id.hashCode().toLong()
            is DateSeparatorItem -> date.time
            is LoadingMoreIndicatorItem -> LOADING_MORE_INDICATOR_STABLE_ID
            is ThreadPlaceholderItem -> THREAD_PLACEHOLDER_STABLE_ID
        }
    }

    public data class DateSeparatorItem(
        val date: Date,
    ) : MessageListItem()

    public data class MessageItem(
        val message: Message,
        val positions: List<MessagePosition> = listOf(),
        val isMine: Boolean = false,
        val messageReadBy: List<ChannelUserRead> = listOf(),
        val isThreadMode: Boolean = false,
        val isMessageRead: Boolean = true,
        val showMessageFooter: Boolean = false,
    ) : MessageListItem() {
        public val isTheirs: Boolean
            get() = !isMine
    }

    public data class TypingItem(
        val users: List<User>,
    ) : MessageListItem()

    public data class ThreadSeparatorItem(
        val date: Date,
        val messageCount: Int,
    ) : MessageListItem()

    public object LoadingMoreIndicatorItem : MessageListItem()

    public object ThreadPlaceholderItem : MessageListItem()

    private companion object {
        private const val TYPING_ITEM_STABLE_ID = 1L
        private const val THREAD_SEPARATOR_ITEM_STABLE_ID = 2L
        private const val LOADING_MORE_INDICATOR_STABLE_ID = 3L
        private const val THREAD_PLACEHOLDER_STABLE_ID = 4L
    }
}