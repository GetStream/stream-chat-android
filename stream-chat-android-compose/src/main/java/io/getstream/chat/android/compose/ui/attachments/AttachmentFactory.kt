package io.getstream.chat.android.compose.ui.attachments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.util.previewText

/**
 * Holds the information required to build an attachment message.
 *
 * @param canHandle Checks the message and returns if the factory can consume it or not.
 * @param previewContent Composable function that allows users to define the content the [AttachmentFactory] will build,
 * using any given [AttachmentState], when the message is displayed in the message input preview, before sending.
 * @param content Composable function that allows users to define the content the [AttachmentFactory] will build using any given
 * [AttachmentState], when the message is displayed in the message list.
 * @param textFormatter The formatter used to get a string representation for the given attachment.
 */
public open class AttachmentFactory constructor(
    public val canHandle: (attachments: List<Attachment>) -> Boolean,
    public val previewContent: (
        @Composable (
            modifier: Modifier,
            attachments: List<Attachment>,
            onAttachmentRemoved: (Attachment) -> Unit,
        ) -> Unit
    )? = null,
    public val content: @Composable (
        modifier: Modifier,
        attachmentState: AttachmentState,
    ) -> Unit,
    public val textFormatter: (attachments: Attachment) -> String = Attachment::previewText,
)
