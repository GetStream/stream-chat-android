package io.getstream.chat.android.compose.state.messages.composer

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.ValidationError

/**
 * Represents the state within the message input.
 *
 * @param inputValue The current text value that's within the input.
 * @param attachments The currently selected attachments.
 * @param action The currently active [MessageAction].
 * @param validationErrors The list of validation errors.
 */
public data class MessageInputState(
    val inputValue: String = "",
    val attachments: List<Attachment> = emptyList(),
    val action: MessageAction? = null,
    val validationErrors: List<ValidationError> = emptyList(),
)
