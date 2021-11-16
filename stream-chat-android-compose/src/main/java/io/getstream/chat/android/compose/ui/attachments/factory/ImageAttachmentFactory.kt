package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentPreviewContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * An [AttachmentFactory] that validates attachments as images and uses [ImageAttachmentContent] to
 * build the UI for the message.
 */
@Suppress("FunctionName")
public fun ImageAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.all { it.isMedia() } },
    previewContent = { modifier, attachments, onAttachmentRemoved ->
        ImageAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
    content = @Composable { modifier, state ->
        ImageAttachmentContent(
            modifier = modifier.size(
                width = ChatTheme.dimens.attachmentsContentImageWidth,
                height = ChatTheme.dimens.attachmentsContentImageHeight
            ),
            attachmentState = state
        )
    },
)
