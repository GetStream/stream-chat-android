package io.getstream.chat.android.compose.state.messages.composer

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageAction

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
    val maxMessageLength: Int = Integer.MAX_VALUE,
)
