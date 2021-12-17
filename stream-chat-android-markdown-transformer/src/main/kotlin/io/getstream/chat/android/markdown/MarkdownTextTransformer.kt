package io.getstream.chat.android.markdown

import android.content.Context
import android.widget.TextView
import com.getstream.sdk.chat.ChatMessageTextTransformer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.Linkify
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

public class MarkdownTextTransformer(context: Context) : ChatMessageTextTransformer {
    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(CorePlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(ImagesPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .build()

    override fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem) {
        markwon.setMarkdown(textView, fixItalicAtEnd(messageItem.message.text))
        Linkify.addLinks(textView)
    }
}
