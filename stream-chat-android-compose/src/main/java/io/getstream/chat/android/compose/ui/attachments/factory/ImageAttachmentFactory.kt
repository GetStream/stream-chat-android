package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentContent
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * An [AttachmentFactory] that validates attachments as images and uses [ImageAttachmentContent] to
 * build the UI for the message.
 */
@Suppress("FunctionName")
public fun ImageAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.all { it.isMedia() } },
    content = @Composable { modifier, state ->
        ImageAttachmentContent(
            modifier = modifier,
            attachmentState = state
        )
    },
)
