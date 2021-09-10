package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent

/**
 * An [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 */
@Suppress("FunctionName")
public fun FileAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.isNotEmpty() },
    content = @Composable { FileAttachmentContent(it) },
)
