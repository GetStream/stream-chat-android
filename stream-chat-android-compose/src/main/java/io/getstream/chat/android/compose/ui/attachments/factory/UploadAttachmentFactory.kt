package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileUploadContent
import io.getstream.chat.android.compose.ui.util.isUploading

/**
 * An [AttachmentFactory] that validates and shows uploading attachments using [FileUploadContent].
 * Has no "preview content", given that this attachment only exists after being sent.
 */
@Suppress("FunctionName")
public fun UploadAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.isUploading() } },
    previewContent = @Composable { _, _, _ -> },
    content = @Composable { modifier, state ->
        FileUploadContent(
            modifier = modifier,
            attachmentState = state
        )
    },
)
