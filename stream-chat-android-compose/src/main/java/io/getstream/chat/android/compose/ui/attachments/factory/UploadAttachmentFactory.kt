package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileUploadContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isUploading

/**
 * An [AttachmentFactory] that validates and shows uploading attachments using [FileUploadContent].
 * Has no "preview content", given that this attachment only exists after being sent.
 */
@Suppress("FunctionName")
public fun UploadAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.isUploading() } },
    content = @Composable { modifier, state ->
        FileUploadContent(
            modifier = modifier
                .wrapContentHeight()
                .width(ChatTheme.dimens.attachmentsContentFileUploadWidth),
            attachmentState = state
        )
    },
)
