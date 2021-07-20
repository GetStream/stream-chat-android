package io.getstream.chat.android.compose.state.messages.attachments

import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.Message

data class AttachmentState(
    val modifier: Modifier,
    val message: Message,
    val onLongItemClick: (Message) -> Unit = {}
)