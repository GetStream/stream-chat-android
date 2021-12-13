package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the selected attachments within the composer, based on if they're images or files.
 *
 * @param attachments List of selected attachments.
 * @param onAttachmentRemoved Handler when the user removes a selected attachment.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun MessageInputAttachments(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val previewFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }

    previewFactory?.previewContent?.invoke(modifier, attachments, onAttachmentRemoved)
}
