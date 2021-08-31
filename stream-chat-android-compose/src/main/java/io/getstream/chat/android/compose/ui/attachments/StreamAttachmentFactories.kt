package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState

public object StreamAttachmentFactories {

    /**
     * The default description length of link attachments. We limit this, because for some links the description
     * can prove to be too long.
     * */
    private const val DEFAULT_LINK_DESCRIPTION_LINE_LIMIT = 5

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * @param linkDescriptionLineLimit - The limit of how long the link attachment descriptions can be.
     * */
    public fun defaultFactories(
        linkDescriptionLineLimit: Int = DEFAULT_LINK_DESCRIPTION_LINE_LIMIT,
    ): List<AttachmentFactory> = listOf(
        LinkAttachmentFactory(linkDescriptionLineLimit),
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
 * */
public abstract class AttachmentFactory(
    public val canHandle: (attachments: List<Attachment>) -> Boolean,
    public val content: @Composable (AttachmentState) -> Unit,
)
