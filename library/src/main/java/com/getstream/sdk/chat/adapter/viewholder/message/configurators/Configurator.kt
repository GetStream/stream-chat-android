package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import io.getstream.chat.android.livedata.utils.MessageListItem

internal interface Configurator {
    fun configure(messageItem: MessageListItem.MessageItem)
}
