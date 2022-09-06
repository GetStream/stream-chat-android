package io.getstream.chat.android.common.model.messsagelist

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.messagelist.MessageFocusState
import io.getstream.chat.android.common.state.messagelist.MessagePosition
import java.util.Date

/**
 * Represents an list item inside a message list.
 */
public sealed class MessageListItem

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
 * @param deletedMessageVisibility The [DeletedMessageVisibility] which determines the behavior of deleted messages.
 * @param focusState The current [MessageFocusState] of the message, used to focus the message in the ui.
 */
public data class MessageItem(
    public val message: Message = Message(),
    public val parentMessageId: String? = null,
    public val isMine: Boolean = false,
    public val isInThread: Boolean = false,
    public val showMessageFooter: Boolean = false,
    public val currentUser: User? = null,
    public val groupPosition: List<MessagePosition> = listOf(MessagePosition.NONE),
    public val isMessageRead: Boolean = false,
    public val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_HIDDEN,
    public val focusState: MessageFocusState? = null
) : MessageListItem()

/**
 * Represents a date separator inside the message list.
 *
 * @param date The date to show on the separator.
 */
public data class DateSeparatorItem(
    val date: Date,
) : MessageListItem()

/**
 * Represents a date separator inside thread messages list.
 *
 * @param date The date show on the separator.
 * @param messageCount Number of messages inside the thread.
 */
public data class ThreadSeparatorItem(
    public val date: Date,
    public val messageCount: Int,
) : MessageListItem()

/**
 * Represents a system message inside the message list.
 *
 * @param message The [Message] to show as the system message inside the list.
 */
public data class SystemMessageItem(
    public val message: Message,
) : MessageListItem()

/**
 * Represents a typing indicator item inside a message list.
 *
 * @param typingUsers The list of the [User]s currently typing a message.
 */
public data class TypingItem(
    public val typingUsers: List<User>,
) : MessageListItem()

