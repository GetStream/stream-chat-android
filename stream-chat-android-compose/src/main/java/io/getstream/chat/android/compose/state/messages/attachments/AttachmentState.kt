package io.getstream.chat.android.compose.state.messages.attachments

import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.items.MessageItem

public data class AttachmentState(
    val modifier: Modifier,
    val message: MessageItem,
    val onLongItemClick: (Message) -> Unit = {},
)
