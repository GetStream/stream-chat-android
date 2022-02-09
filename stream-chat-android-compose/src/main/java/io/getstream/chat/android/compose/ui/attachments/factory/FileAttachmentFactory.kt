package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentPreviewContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 */
@Suppress("FunctionName")
public fun FileAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments ->
        attachments.any {
            it.uploadId != null ||
                it.upload != null ||
                it.type == ModelType.attach_file ||
                it.type == ModelType.attach_video ||
                it.type == ModelType.attach_audio
        }
    },
    previewContent = @Composable { modifier, attachments, onAttachmentRemoved ->
        FileAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
    content = @Composable { modifier, state ->
        FileAttachmentContent(
            modifier = modifier
                .wrapContentHeight()
                .width(ChatTheme.dimens.attachmentsContentFileWidth),
            attachmentState = state
        )
    },
)
