package io.getstream.chat.android.ui.transformer

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.Linkify

/**
 * AutoLinkable implementation of [ChatMessageTextTransformer] that makes [TextView] links clickable after applying the transformer.
 *
 * By default our SDK text views don't have `android:autoLink` set due to a limitation in Markdown linkify implementation.
 */
public class AutoLinkableTextTransformer(public val transformer: (textView: TextView, messageItem: MessageListItem.MessageItem) -> Unit) :
    ChatMessageTextTransformer {

    override fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem) {
        transformer(textView, messageItem)
        Linkify.addLinks(textView)
    }
}
