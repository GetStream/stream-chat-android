package io.getstream.chat.android.compose.state.messages.list

import io.getstream.chat.android.client.models.Message
import java.util.Date

/**
 * Represents the type of an item that we're showing in the message list.
 */
public sealed class MessageListItemState

/**
 * Represents a date separator in the list.
 *
 * @param date The date of the message that we're showing a separator for.
 */
public data class DateSeparatorState(val date: Date) : MessageListItemState()

/**
 * Represents a separator between thread parent message and thread replies.
 *
 * @param replyCount The number of thread replies to the message.
 */
public data class ThreadSeparatorState(val replyCount: Int) : MessageListItemState()

/**
 * Represents each message item we show in the list of messages.
 *
 * @param message The message to show.
 * @param groupPosition The position of this message in a group, if it belongs to one.
 * @param parentMessageId The id of the parent message, when we're in a thread.
 * @param isMine If the message is of the current user or someone else.
 * @param isFocused If the message is being focused currently.
 * @param isInThread If the message is being displayed in a thread.
 */
public data class MessageItemState(
    val message: Message,
    val groupPosition: MessageItemGroupPosition = MessageItemGroupPosition.None,
    val parentMessageId: String? = null,
    val isMine: Boolean = false,
    val isFocused: Boolean = false,
    val isInThread: Boolean = false,
) : MessageListItemState()
