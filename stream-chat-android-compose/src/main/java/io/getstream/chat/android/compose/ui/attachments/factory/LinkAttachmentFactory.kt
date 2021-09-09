package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.LinkAttachmentContent
import io.getstream.chat.android.compose.ui.util.hasLink

/**
 * An [AttachmentFactory] that validates attachments as images and uses [LinkAttachmentContent] to
 * build the UI for the message.
 *
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 */
@Suppress("FunctionName")
public fun LinkAttachmentFactory(
    linkDescriptionMaxLines: Int,
): AttachmentFactory = AttachmentFactory(
    canHandle = { links -> links.any { it.hasLink() && it.type != ModelType.attach_giphy } },
    content = @Composable { LinkAttachmentContent(it, linkDescriptionMaxLines) },
)
