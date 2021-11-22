package io.getstream.chat.android.compose.state.messages.composer

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageAction

/**
 * The default allowed number of characters in a message.
 */
private const val DEFAULT_MAX_MESSAGE_LENGTH: Int = 5000

/**
 * Represents the state within the message input.
 *
 * @param inputValue The current text value that's within the input.
 * @param attachments The currently selected attachments.
 * @param action The currently active [MessageAction].
 * @param maxMessageLength The maximum allowed message length.
 */
public data class MessageInputState(
    val inputValue: String = "",
    val attachments: List<Attachment> = emptyList(),
    val action: MessageAction? = null,
    val maxMessageLength: Int = DEFAULT_MAX_MESSAGE_LENGTH,
)
