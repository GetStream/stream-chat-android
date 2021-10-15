package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasLink

/**
 * Represents the content that's shown in message attachments. We decide if we need to show link previews or other
 * attachments.
 *
 * @param message The message that contains the attachments.
 * @param onLongItemClick Handler for long item taps on this content.
 * @param onImagePreviewResult Handler when the user selects a message option in the Image Preview screen.
 */
@Composable
public fun MessageAttachmentsContent(
    message: Message,
    onLongItemClick: (Message) -> Unit,
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
) {
    if (message.attachments.isNotEmpty()) {
        val (links, attachments) = message.attachments.partition { it.hasLink() && it.type != ModelType.attach_giphy }

        val linkFactory = if (links.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(links) }
        } else {
            null
        }

        val attachmentFactory = if (attachments.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }
        } else {
            null
        }

        val attachmentState = AttachmentState(
            message = message,
            onLongItemClick = onLongItemClick,
            onImagePreviewResult = onImagePreviewResult
        )

        if (attachmentFactory != null) {
            attachmentFactory.content(Modifier.padding(2.dp), attachmentState)
        } else if (linkFactory != null) {
            linkFactory.content(Modifier.padding(8.dp), attachmentState)
        }
    }
}
