package io.getstream.chat.android.compose.state.messages.attachments

import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.items.MessageItem

/**
 * Represents the state of Attachment items, used to render and add handlers required for the attachment to work.
 *
 * @param modifier Modifier for styling.
 * @param messageItem Data that represents the message information.
 * @param onLongItemClick Handler for a long click on the message item.
 */
public data class AttachmentState(
    val modifier: Modifier,
    val messageItem: MessageItem,
    val onLongItemClick: (Message) -> Unit = {},
)
