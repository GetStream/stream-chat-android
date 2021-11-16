package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentPreviewContent

/**
 * An [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 */
@Suppress("FunctionName")
public fun FileAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.uploadId != null || it.upload != null } },
    previewContent = @Composable { modifier, attachments, onAttachmentRemoved ->
        FileAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
    content = @Composable { modifier, state ->
        FileAttachmentContent(
            modifier = modifier,
            attachmentState = state
        )
    },
)
