package io.getstream.chat.android.common.messagelist

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.model.messsagelist.MessageListItem
import io.getstream.chat.android.common.state.messagelist.NewMessageState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageState

/**
 * Holds the state of the messages list screen.
 *
 * @param messages The list of [MessageListItem]s to be shown in the list.
 * @param endOfNewMessagesReached Whether the user has reached the newest message or not.
 * @param endOfOldMessagesReached Whether the user has reached the older message or not.
 * @param isLoading Whether the initial loading is in progress or not.
 * @param isLoadingNewerMessages Whether loading of a page with newer messages is in progress or not.
 * @param isLoadingOlderMessages Whether loading of a page with older messages is in progress or not.
 * @param currentUser The current logged in [User].
 * @param parentMessageId The [Message] id if we are in a thread, null otherwise.
 * @param unreadCount Count of unread messages in channel or thread.
 * @param typingUsers The list of the users currently typing a message.
 * @param newMessageState The [NewMessageState] of the newly received message.
 * @param selectedMessageState The current [SelectedMessageState].
 */
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