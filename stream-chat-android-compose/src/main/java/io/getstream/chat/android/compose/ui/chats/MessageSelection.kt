package io.getstream.chat.android.compose.ui.chats

/**
 * Represents the selection of a message within a channel.
 */
public data class MessageSelection(
    /**
     * The ID of the selected channel, or `null` if no channel is selected.
     */
    val channelId: String? = null,
    /**
     * The ID of a specific message, or `null` if navigating to a channel without a pre-selected message.
     */
    val messageId: String? = null,
    /**
     * The ID of the parent message (for threads), or `null` if not in a thread.
     */
    val parentMessageId: String? = null,
)
