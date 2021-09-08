package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.factory.FileAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.GiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.ImageAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.LinkAttachmentFactory

public object StreamAttachmentFactories {

    /**
     * The default max length of the link attachments description. We limit this, because for some links the description
     * can be too long.
     */
    private const val DEFAULT_LINK_DESCRIPTION_MAX_LINES = 5

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * @param linkDescriptionMaxLines - The limit of how long the link attachment descriptions can be.
     * @return A [List] of various [AttachmentFactory] instances that provide different attachments support.
     */
    public fun defaultFactories(
        linkDescriptionMaxLines: Int = DEFAULT_LINK_DESCRIPTION_MAX_LINES,
    ): List<AttachmentFactory> = listOf(
        LinkAttachmentFactory(linkDescriptionMaxLines),
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
