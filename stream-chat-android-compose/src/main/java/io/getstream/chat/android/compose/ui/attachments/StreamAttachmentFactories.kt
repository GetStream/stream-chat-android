package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState

public object StreamAttachmentFactories {

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * Uses the functions below to display the UI.
     */
    public val defaultFactories: List<AttachmentFactory> = listOf(
        LinkAttachmentFactory(),
        GiphyAttachmentFactory(),
        ImageAttachmentFactory(),
        FileAttachmentFactory()
    )
}

/**
 * Holds the information required to build an attachment message.
 *
 * @param canHandle - Checks the message and returns if the factory can consume it or not.
 * @param content - Composable function that allows users to define the content the [AttachmentFactory] will build using any given
 * [AttachmentState].
 */
public abstract class AttachmentFactory(
    public val canHandle: (attachments: List<Attachment>) -> Boolean,
    public val content: @Composable (AttachmentState) -> Unit,
)
