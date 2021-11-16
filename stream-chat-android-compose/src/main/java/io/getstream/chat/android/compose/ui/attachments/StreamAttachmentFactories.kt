package io.getstream.chat.android.compose.ui.attachments

import io.getstream.chat.android.compose.ui.attachments.factory.FileAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.GiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.ImageAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.LinkAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UploadAttachmentFactory

/**
 * Provides different attachment factories that build custom message content based on a given attachment.
 */
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
        UploadAttachmentFactory(),
        LinkAttachmentFactory(linkDescriptionMaxLines),
        GiphyAttachmentFactory(),
        ImageAttachmentFactory(),
        FileAttachmentFactory(),
    )
}
