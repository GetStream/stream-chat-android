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
 * */
public abstract class AttachmentFactory {
    /**
     * Returns if this specific factory can handle a specific message.
     *
     * @param message - The message to check.
     * @return a boolean value, if we can consume the message and render UI.
     * */
    public fun canHandle(message: Message): Boolean {
        return message.attachments.isNotEmpty() && predicate(message)
    }

    /**
     * Checks the message and returns if the factory can consume it or not.
     *
     * @param message - The message to validate
     * @return - a Boolean value
     * */
    public abstract fun predicate(message: Message): Boolean

    /**
     * Composable function that allows users to define the content the [AttachmentFactory] will build using any given
     * [attachmentState].
     *
     * @param attachmentState - The state for this attachment to use for the UI.
     * */
    @Composable
    public abstract fun Content(attachmentState: AttachmentState)
}
