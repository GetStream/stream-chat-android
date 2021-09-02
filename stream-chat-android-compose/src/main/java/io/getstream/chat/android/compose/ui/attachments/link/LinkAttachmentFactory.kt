package io.getstream.chat.android.compose.ui.attachments.link

import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.util.hasLink

/**
 * An extension of the [AttachmentFactory] that validates attachments as images and uses [LinkAttachmentContent] to
 * build the UI for the message.
 *
 * @param linkDescriptionLineLimit - The limit of how many lines we show for the link description.
 */
public class LinkAttachmentFactory(
    linkDescriptionLineLimit: Int,
) : AttachmentFactory(
    canHandle = { links -> links.any { it.hasLink() && it.type != ModelType.attach_giphy } },
    content = @Composable { LinkAttachmentContent(it, linkDescriptionLineLimit) }
)
