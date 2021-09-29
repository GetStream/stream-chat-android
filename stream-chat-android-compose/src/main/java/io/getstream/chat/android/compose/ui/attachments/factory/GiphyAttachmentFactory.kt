package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent

/**
 * An [AttachmentFactory] that validates and shows Giphy attachments using [GiphyAttachmentContent].
 */
@Suppress("FunctionName")
public fun GiphyAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
    content = @Composable { modifier, state ->
        GiphyAttachmentContent(
            modifier = modifier,
            attachmentState = state
        )
    },
)
