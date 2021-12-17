package com.getstream.sdk.chat

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem

public fun interface ChatMessageTextTransformer {
    public fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem)
}
