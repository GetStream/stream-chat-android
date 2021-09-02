package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentContent
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * An extension of the [AttachmentFactory] that validates attachments as images and uses [ImageAttachmentContent] to
 * build the UI for the message.
 * */
public class ImageAttachmentFactory : AttachmentFactory(
    canHandle = { attachments -> attachments.all { it.isMedia() } },
    content = @Composable { ImageAttachmentContent(it) }
)
