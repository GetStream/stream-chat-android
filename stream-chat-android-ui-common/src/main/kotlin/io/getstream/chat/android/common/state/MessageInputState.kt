package io.getstream.chat.android.common.state

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User

/**
 * Represents the state within the message input.
 *
 * @param inputValue The current text value that's within the input.
 * @param attachments The currently selected attachments.
 * @param action The currently active [MessageAction].
 * @param validationErrors The list of validation errors.
 * @param mentionSuggestions The list of users that can be used to autocomplete the mention.
 * @param commandSuggestions The list of commands to be displayed in the command suggestion popup.
 * @param coolDownTimer The amount of time left until the user is allowed to send the next message.
 * @param messageMode The message mode that's currently active.
 * @param alsoSendToChannel If the message will be shown in the channel after it is sent.
 */
public data class MessageInputState(
    val inputValue: String = "",
    val attachments: List<Attachment> = emptyList(),
    val action: MessageAction? = null,
    val validationErrors: List<ValidationError> = emptyList(),
    val mentionSuggestions: List<User> = emptyList(),
    val coolDownTimer: Int = 0,
    val commandSuggestions: List<Command> = emptyList(),
    val messageMode: MessageMode = MessageMode.Normal,
    val alsoSendToChannel: Boolean = false,
)
