package com.getstream.sdk.chat.view.messages

import com.getstream.sdk.chat.adapter.MessageListItem

/**
 * MessageListItemWrapper wraps a list of MessageListItem with a few extra fields.
 */
public data class MessageListItemWrapper(
    val items: List<MessageListItem> = listOf(),
    var loadingMore: Boolean = false,
    val hasNewMessages: Boolean = false,
    val isTyping: Boolean = false,
    val isThread: Boolean = false
)
