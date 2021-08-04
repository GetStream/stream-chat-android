package io.getstream.chat.android.compose.state.messages.items

import io.getstream.chat.android.client.models.Message

public data class MessageItem(
    val message: Message,
    val position: MessageItemGroupPosition,
    val parentMessageId: String? = null,
    val isMine: Boolean = false
)
