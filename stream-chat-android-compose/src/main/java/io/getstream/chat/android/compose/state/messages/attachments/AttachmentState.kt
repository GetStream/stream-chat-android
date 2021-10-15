package io.getstream.chat.android.compose.state.messages.attachments

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult

/**
 * Represents the state of Attachment items, used to render and add handlers required for the attachment to work.
 *
 * @param message Data that represents the message information.
 * @param onLongItemClick Handler for a long click on the message item.
 * @param onImagePreviewResult Handler when the user selects an action to scroll to and focus an image.
 */
public data class AttachmentState(
    val message: Message,
    val onLongItemClick: (Message) -> Unit = {},
    val onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
)
