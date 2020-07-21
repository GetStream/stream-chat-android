package com.getstream.sdk.chat.view.messages

import com.getstream.sdk.chat.adapter.MessageListItem

data class MessageListItemWrapper(
    var loadingMore: Boolean = false,
    val hasNewMessages: Boolean = false,
    var listEntities: List<MessageListItem> = listOf(),
    val isTyping: Boolean = false,
    val isThread: Boolean = false
)
