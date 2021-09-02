package io.getstream.chat.android.compose.ui.attachments.file

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory

/**
 * An extension of the [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 */
public class FileAttachmentFactory : AttachmentFactory(
    canHandle = { attachments -> attachments.isNotEmpty() },
    content = @Composable { FileAttachmentContent(it) }
)
