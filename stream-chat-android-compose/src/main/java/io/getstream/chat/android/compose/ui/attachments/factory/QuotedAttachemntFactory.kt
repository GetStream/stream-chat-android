package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentQuotedContent
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentQuotedContent
import io.getstream.chat.android.compose.ui.util.hasLink
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * TODO
 */
@Suppress("FunctionName")
public fun QuotedAttachmentFactory() : AttachmentFactory = AttachmentFactory(
    canHandle = { it.isNotEmpty() },
    content = content@ @Composable { modifier, attachmentState ->
        val attachment = attachmentState.message.attachments.firstOrNull() ?: return@content

        val isFile = attachment.uploadId != null
            || attachment.upload != null
            || attachment.type == ModelType.attach_file
            || attachment.type == ModelType.attach_video
            || attachment.type == ModelType.attach_audio

        val isImage = attachment.isMedia()
        val isLink = attachment.hasLink()

        when {
            isImage || isLink -> ImageAttachmentQuotedContent(modifier = modifier, attachment = attachment)
            isFile -> FileAttachmentQuotedContent(modifier = modifier, attachment = attachment)
        }
    }
)
