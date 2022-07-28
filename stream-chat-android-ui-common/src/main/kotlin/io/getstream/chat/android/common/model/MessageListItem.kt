package io.getstream.chat.android.common.model

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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


public sealed class MessageListItem

public data class MessageItem(
    public val message: Message = Message(),
    public val messagePosition: MessagePosition = MessagePosition.NONE,
    public val parentMessageId: String? = null,
    public val isMine: Boolean = false,
    public val isInThread: Boolean = false,
    public val showMessageFooter: Boolean = false,
    public val currentUser: User? = null,
    public val groupPosition: MessagePosition = MessagePosition.NONE,
    public val isMessageRead: Boolean = false,
    public val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_HIDDEN
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

public enum class MessagePosition {
    TOP,
    MIDDLE,
    BOTTOM,
    NONE
}