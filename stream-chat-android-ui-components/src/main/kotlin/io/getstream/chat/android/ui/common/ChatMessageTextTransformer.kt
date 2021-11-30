package io.getstream.chat.android.ui.common

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem

public fun interface ChatMessageTextTransformer {
    public fun transform(textView: TextView, messageItem: MessageListItem.MessageItem)
}

internal class DefaultMessageTextTransformer: ChatMessageTextTransformer {
    override fun transform(textView: TextView, messageItem: MessageListItem.MessageItem) {
        // no-op transformer
    }
}