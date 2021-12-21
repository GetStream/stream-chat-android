package io.getstream.chat.android.ui.transformer

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.ChatUI

/**
 * Default no-op implementation of [ChatMessageTextTransformer] that does nothing.
 *
 * Currently, it bypass to [ChatUI.markdown] instead of being no-op to provide compatibility
 * till ChatUI.markdown is completely removed.
 */
internal class DefaultChatTextTransformer : ChatMessageTextTransformer {
    override fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem) {

        // Bypass to markdown by default for backwards compatibility.
        ChatUI.markdown.setText(textView, messageItem.message.text)
    }
}
