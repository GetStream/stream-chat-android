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

package io.getstream.chat.android.ui.feature.messages.list.adapter

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import java.util.Date

/**
 * [MessageListItem] represents elements that are displayed in a [MessageListView].
 * There are the following subclasses of the [MessageListItem] available:
 * - [DateSeparatorItem]
 * - [MessageItem]
 * - [TypingItem]
 * - [ThreadSeparatorItem]
 * - [LoadingMoreIndicatorItem]
 * - [ThreadPlaceholderItem]
 * - [UnreadSeparatorItem]
 * - [StartOfTheChannelItem]
 */
public sealed class MessageListItem {

    public fun getStableId(): Long {
        return when (this) {
            is TypingItem -> TYPING_ITEM_STABLE_ID
            is ThreadSeparatorItem -> THREAD_SEPARATOR_ITEM_STABLE_ID
            is MessageItem -> uniqueIdentifier()
            is DateSeparatorItem -> date.time
            is LoadingMoreIndicatorItem -> LOADING_MORE_INDICATOR_STABLE_ID
            is ThreadPlaceholderItem -> THREAD_PLACEHOLDER_STABLE_ID
            is UnreadSeparatorItem -> UNREAD_SEPARATOR_STABLE_ID
            is StartOfTheChannelItem -> START_OF_THE_CHANNEL_STABLE_ID
        }
    }

    public abstract fun stringify(): String

    /**
     * Represent a date separator item in a [MessageListView].
     *
     * @property date The date that should be displayed in the date separator.
     */
    public data class DateSeparatorItem(
        val date: Date,
    ) : MessageListItem() {
        override fun stringify(): String {
            return "DateItem(date=$date)"
        }
    }

    /**
     * Represent a message item in a [MessageListView].
     *
     * @property message The message that should be displayed in the message item.
     * @property position The positions of the message item in a group.
     * @property isMine True if the message is sent by the current user, otherwise false.
     * @property messageReadBy The list of users that already read the message.
     * @property isThreadMode True if the message is in a thread mode, otherwise false.
     * @property isMessageRead True if the message has been read or not.
     * @property isMessageDelivered Whether the message has been delivered or not.
     * @property showMessageFooter True if the message footer should be displayed, otherwise false.
     * @property isTheirs True if the message is sent by another user, otherwise false.
     * @property showMessageFooter True if the message footer should be displayed, otherwise false.
     * @property showOriginalText If the original text of the message should be shown in the UI instead of its
     * translation (if the message was auto-translated).
     */
    public data class MessageItem(
        val message: Message,
        val position: MessagePosition = MessagePosition.NONE,
        val isMine: Boolean = false,
        val messageReadBy: List<ChannelUserRead> = listOf(),
        val isThreadMode: Boolean = false,
        val isMessageRead: Boolean = true,
        val isMessageDelivered: Boolean = false,
        val showMessageFooter: Boolean = false,
        val showOriginalText: Boolean = false,
    ) : MessageListItem() {
        public val isTheirs: Boolean
            get() = !isMine

        /**
         * Identifier of message.
         * It is an unique identifier of message in the channel that doesn't change even if the message content changes.
         */
        internal fun uniqueIdentifier(): Long = message.identifierHash()

        override fun stringify(): String {
            return "MessageItem(message=${message.text})"
        }
    }

    /**
     * Represent a typing item in a [MessageListView].
     *
     * @property users The list of users that are currently typing.
     */
    public data class TypingItem(
        val users: List<User>,
    ) : MessageListItem() {
        override fun stringify(): String {
            return "TypingItem(users.size=${users.size})"
        }
    }

    /**
     * Represent a thread separator item in a [MessageListView].
     *
     * @property date The date that should be displayed in the thread separator.
     * @property messageCount The number of messages in the thread.
     */
    public data class ThreadSeparatorItem(
        val date: Date,
        val messageCount: Int,
    ) : MessageListItem() {
        override fun stringify(): String {
            return "ThreadSeparatorItem(messageCount=$messageCount, date=$date)"
        }
    }

    /**
     * Represent a loading more indicator item in a [MessageListView].
     */
    public data object LoadingMoreIndicatorItem : MessageListItem() {
        override fun stringify(): String = toString()
    }

    /**
     * Represent a thread placeholder item in a [MessageListView].
     */
    public data object ThreadPlaceholderItem : MessageListItem() {
        override fun stringify(): String = LoadingMoreIndicatorItem.toString()
    }

    /**
     * Represent an unread separator item in a [MessageListView].
     */
    public data class UnreadSeparatorItem(
        val unreadCount: Int,
    ) : MessageListItem() {

        override fun stringify(): String {
            return "UnreadItem(unreadCount=$unreadCount)"
        }
    }

    /**
     * Represent the start of the channel in a [MessageListView].
     *
     * @property channel The [Channel] this message list belongs to.
     */
    public data class StartOfTheChannelItem(
        val channel: Channel,
    ) : MessageListItem() {

        override fun stringify(): String {
            return "StartOfTheChannelItem(channel.name=${channel.name})"
        }
    }

    private companion object {
        private const val TYPING_ITEM_STABLE_ID = 1L
        private const val THREAD_SEPARATOR_ITEM_STABLE_ID = 2L
        private const val LOADING_MORE_INDICATOR_STABLE_ID = 3L
        private const val THREAD_PLACEHOLDER_STABLE_ID = 4L
        private const val UNREAD_SEPARATOR_STABLE_ID = 5L
        private const val START_OF_THE_CHANNEL_STABLE_ID = 6L
    }
}
