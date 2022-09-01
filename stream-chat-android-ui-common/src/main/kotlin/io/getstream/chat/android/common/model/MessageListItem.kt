package io.getstream.chat.android.common.model

import android.content.ClipData
import android.content.ClipboardManager
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import java.util.Date

public data class MessageListState(
    public val messages: List<MessageListItem> = emptyList(),
    public val endOfNewMessagesReached: Boolean = false,
    public val endOfOldMessagesReached: Boolean = false,
    public val isLoading: Boolean = false,
    public val isLoadingNewerMessages: Boolean = false,
    public val isLoadingOlderMessages: Boolean = false,
    public val currentUser: User? = User(),
    public val parentMessageId: String? = null,
    public val unreadCount: Int = 0,
    public val typingUsers: List<User> = emptyList(),
    public val newMessageState: NewMessageState? = null,
    public val selectedMessageState: SelectedMessageState? = null
)

// TODO
/**
 * Represents the state when a new message arrives to the channel.
 */
public sealed class NewMessageState

/**
 * If the message is our own (we sent it), we scroll to the bottom of the list.
 */
public object MyOwn : NewMessageState()

/**
 * If the message is someone else's (we didn't send it), we show a "New message" bubble.
 */
public object Other : NewMessageState()

// TODO
public sealed class MessageListItem

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

public data class DateSeparatorItem(
    val date: Date,
) : MessageListItem()

public data class ThreadSeparatorItem(
    public val date: Date,
    public val messageCount: Int,
) : MessageListItem()

public data class SystemMessageItem(
    public val message: Message,
) : MessageListItem()

public data class TypingItem(
    public val typingUsers: List<User>,
) : MessageListItem()

// TODO
public enum class MessagePosition {
    TOP,
    MIDDLE,
    BOTTOM,
    NONE
}

// TODO
/**
 * A handler to determine the position of a message inside a group.
 */
public fun interface MessagePositionHandler {
    /**
     * Determines the position of a message inside a group.
     *
     * @param prevMessage The previous [Message] in the list.
     * @param message The current [Message] in the list.
     * @param nextMessage The next [Message] in the list.
     * @param isAfterDateSeparator If a date separator was added before the current [Message].
     *
     * @return The position of the current message inside the group.
     */
    public fun handleMessagePosition(
        prevMessage: Message?,
        message: Message,
        nextMessage: Message?,
        isAfterDateSeparator: Boolean,
    ): List<MessagePosition>

    public companion object {
        /**
         * The default implementation of the [MessagePositionHandler] interface which can be taken
         * as a reference when implementing a custom one.
         *
         * @return The default implementation of [MessagePositionHandler].
         */
        internal fun defaultHandler(): MessagePositionHandler {
            return MessagePositionHandler { prevMessage: Message?, message: Message, nextMessage: Message?, isAfterDateSeparator: Boolean ->
                val prevUser = prevMessage?.user
                val user = message.user
                val nextUser = nextMessage?.user

                val position = when {
                    prevUser != user && nextUser == user && isAfterDateSeparator -> MessagePosition.TOP
                    prevUser == user && nextUser == user && !isAfterDateSeparator -> MessagePosition.MIDDLE
                    prevUser == user && nextUser != user -> MessagePosition.BOTTOM
                    else -> MessagePosition.NONE
                }

                listOf(position)
            }
        }
    }
}

// TODO
/**
 * A SAM designed to evaluate if a date separator should be added between messages.
 */
public fun interface DateSeparatorHandler {
    public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean
}

/**
 * Represents the message focus state, in case the user jumps to a message.
 */
public sealed class MessageFocusState

/**
 * Represents the state when the message is currently being focused.
 */
public object MessageFocused : MessageFocusState()

/**
 * Represents the state when we've removed the focus from the message.
 */
public object MessageFocusRemoved : MessageFocusState()

// TODO
/**
 * Represents a state when a message or its reactions were selected.
 *
 * @param message The selected message.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
 */
public sealed class SelectedMessageState(public val message: Message, public val ownCapabilities: Set<String>)

/**
 * Represents a state when a message was selected.
 */
public class SelectedMessageOptionsState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when message reactions were selected.
 */
public class SelectedMessageReactionsState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when the show more reactions button was clicked.
 */
public class SelectedMessageReactionsPickerState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when the moderated message was selected.
 */
public class SelectedMessageFailedModerationState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

// TODO
/**
 * Abstraction over the [ClipboardHandlerImpl] that allows users to copy messages.
 */
public fun interface ClipboardHandler {

    /**
     * @param message The message to copy.
     */
    public fun copyMessage(message: Message)
}

/**
 * A simple implementation that relies on the [clipboardManager] to copy messages.
 *
 * @param clipboardManager System service that allows for clipboard operations, such as putting
 * new data on the clipboard.
 */
public class ClipboardHandlerImpl(private val clipboardManager: ClipboardManager) : ClipboardHandler {

    /**
     * Allows users to copy the message text.
     *
     * @param message Message to copy the text from.
     */
    override fun copyMessage(message: Message) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", message.text))
    }
}

