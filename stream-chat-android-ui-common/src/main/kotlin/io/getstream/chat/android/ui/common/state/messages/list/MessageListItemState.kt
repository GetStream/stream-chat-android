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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Represents a list item inside a message list.
 */
public sealed class MessageListItemState {
    /**
     * A unique identifier for this item.
     */
    @InternalStreamChatApi
    public val id: String get() = when (this) {
        is HasMessageListItemState -> message.id
        is DateSeparatorItemState -> "date-separator-${date.time}"
        is ThreadDateSeparatorItemState -> "thread-date-separator-${date.time}"
        is TypingItemState -> "typing-indicator"
        is EmptyThreadPlaceholderItemState -> "empty-thread-placeholder"
        is UnreadSeparatorItemState -> "unread-separator"
        is StartOfTheChannelItemState -> "start-of-the-channel"
    }
}

/**
 * Represents either regular or system message item inside a message list.
 */
public sealed class HasMessageListItemState : MessageListItemState() {

    /**
     * The [Message] to show in the list.
     */
    public abstract val message: Message
}

/**
 * Represents a message item inside the messages list.
 *
 * @param message The [Message] to show in the list.
 * @param parentMessageId The id of the parent [Message] if the message is inside a thread.
 * @param isMine Whether the message is sent by the current user or not.
 * @param isInThread Whether the message is inside a thread or not.
 * @param showMessageFooter Whether we need to show the message footer or not.
 * @param currentUser The currently logged in user.
 * @param groupPosition The [MessagePosition] of the item inside a group.
 * @param isMessageRead Whether the message has been read or not.
 * @param deletedMessageVisibility The [DeletedMessageVisibility] which determines the visibility of deleted messages in
 * the UI.
 * @param focusState The current [MessageFocusState] of the message, used to focus the message in the ui.
 * @param messageReadBy The list of [ChannelUserRead] for the message.
 * @param showOriginalText If the original text of the message should be shown in the UI instead of its translation (if
 * the message was auto-translated).
 * @param ownCapabilities The capabilities of the current user in the channel.
 */
public data class MessageItemState(
    public override val message: Message = Message(),
    public val parentMessageId: String? = null,
    public val isMine: Boolean = false,
    public val isInThread: Boolean = false,
    public val showMessageFooter: Boolean = false,
    public val currentUser: User? = null,
    public val groupPosition: List<MessagePosition> = listOf(MessagePosition.NONE),
    public val isMessageRead: Boolean = false,
    public val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_HIDDEN,
    public val focusState: MessageFocusState? = null,
    public val messageReadBy: List<ChannelUserRead> = emptyList(),
    public val showOriginalText: Boolean = false,
    public val ownCapabilities: Set<String>,
) : HasMessageListItemState()

/**
 * Represents a date separator inside the message list.
 *
 * @param date The date to show on the separator.
 */
public data class DateSeparatorItemState(
    val date: Date,
) : MessageListItemState()

/**
 * Represents a date separator inside thread messages list.
 *
 * @param date The date show on the separator.
 * @param replyCount Number of messages inside the thread.
 */
public data class ThreadDateSeparatorItemState(
    public val date: Date,
    public val replyCount: Int,
) : MessageListItemState()

/**
 * Represents a system message inside the message list.
 *
 * @param message The [Message] to show as the system message inside the list.
 */
public data class SystemMessageItemState(
    public override val message: Message,
) : HasMessageListItemState()

/**
 * Represents a moderated message inside the message list.
 *
 * @param message The [Message] that was moderated.
 */
public data class ModeratedMessageItemState(
    public override val message: Message,
) : HasMessageListItemState()

/**
 * Represents a typing indicator item inside a message list.
 *
 * @param typingUsers The list of the [User]s currently typing a message.
 */
public data class TypingItemState(
    public val typingUsers: List<User>,
) : MessageListItemState()

/**
 * Represents an empty thread placeholder item inside thread messages list.
 */
public data object EmptyThreadPlaceholderItemState : MessageListItemState()

/**
 * Represents an unread separator item inside a message list.
 *
 * @param unreadCount The number of unread messages.
 */
public data class UnreadSeparatorItemState(
    val unreadCount: Int,
) : MessageListItemState()

/**
 * Represents the start of the channel inside a message list.
 *
 * @param channel The [Channel] this message list belongs to.
 */
public data class StartOfTheChannelItemState(
    val channel: Channel,
) : MessageListItemState()

/**
 * Returns a string representation of the [MessageListItemState].
 */
@InternalStreamChatApi
public fun MessageListItemState.stringify(): String = when (this) {
    is MessageItemState -> "MessageItemState(message.text: ${message.text}, isMine: $isMine)"
    is DateSeparatorItemState -> "DateSeparatorItemState(date: $date)"
    is ThreadDateSeparatorItemState -> "ThreadDateSeparatorItemState(date: $date, replyCount: $replyCount)"
    is SystemMessageItemState -> "SystemMessageItemState(message.text: ${message.text})"
    is ModeratedMessageItemState -> "ModeratedMessageItemState(message.text: ${message.text})"
    is TypingItemState -> "TypingItemState(typingUsers.size: ${typingUsers.size})"
    is UnreadSeparatorItemState -> "UnreadSeparatorItemState(unreadCount: $unreadCount)"
    is StartOfTheChannelItemState -> "StartOfTheChannelItemState(channel.name: ${channel.name})"
    is EmptyThreadPlaceholderItemState -> "EmptyThreadPlaceholderItemState"
}
