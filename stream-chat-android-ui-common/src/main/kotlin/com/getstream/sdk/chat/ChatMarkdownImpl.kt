package com.getstream.sdk.chat

import android.content.Context
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

public class ChatMarkdownImpl(context: Context?) : ChatMarkdown {
    private val markwon: Markwon = Markwon.builder(context!!)
        .usePlugin(CorePlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(ImagesPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .build()

    override fun setText(textView: TextView, text: String) {
        markwon.setMarkdown(textView, text)
    }
}
