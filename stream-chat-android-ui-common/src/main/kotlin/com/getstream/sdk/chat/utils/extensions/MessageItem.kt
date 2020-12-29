package com.getstream.sdk.chat.utils.extensions

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun MessageListItem.MessageItem.isBottomPosition(): Boolean {
    return MessageListItem.Position.BOTTOM in positions
}

@InternalStreamChatApi
public fun MessageListItem.MessageItem.isNotBottomPosition(): Boolean {
    return !isBottomPosition()
}
