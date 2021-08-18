package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState

public object StreamAttachmentFactories {

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * Uses the functions below to display the UI.
     * */
    public val defaultFactories: List<AttachmentFactory> = listOf(
        LinkAttachmentFactory(),
        ImageAttachmentFactory(),
        FileAttachmentFactory()
    )
}

/**
 * Holds the information required to build an attachment message.
 *
 * @param factory - Function that provides a modifier and a message, to show the attachment.
 * @param predicate - Function that checks the message and returns if the factory can consume it or
 * not.
 * */
public abstract class AttachmentFactory(
    public val factory: @Composable (AttachmentState) -> Unit,
    private val predicate: (Message) -> Boolean,
) {
    /**
     * Returns if this specific factory can handle a specific message.
     *
     * @param message - The message to check.
     * @return a boolean value, if we can consume the message and render UI.
     * */
    public fun canHandle(message: Message): Boolean {
        return message.attachments.isNotEmpty() && predicate(message)
    }
}
