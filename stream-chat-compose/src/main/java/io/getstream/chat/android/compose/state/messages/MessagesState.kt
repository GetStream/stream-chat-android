package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

/**
 * UI representation of the Conversation/Messages screen. Holds all the data required to show messages.
 *
 * @param isLoading - If we're loading (initial load).
 * @param isLoadingMore - If we're loading more data (pagination).
 * @param endOfMessages - If we're at the end of messages (to stop pagination).
 * @param messages - Messages to represent in the list.
 * @param selectedMessage - Currently selected message, to show the overlay.
 * @param currentUser - The data of the current user, required for various UI states.
 * @param newMessageState - The state that represents any new messages.
 * */
data class MessagesState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endOfMessages: Boolean = false,
    val messages: List<Message> = emptyList(),
    val selectedMessage: Message? = null,
    val currentUser: User? = null,
    val newMessageState: NewMessageState? = null
)