/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.markdown

import android.content.Context
import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.Linkify
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/**
 * Markdown based implementation of [ChatMessageTextTransformer] that parses the message text as Markdown
 * and apply it to [TextView].
 */
public class MarkdownTextTransformer(context: Context) : ChatMessageTextTransformer {
    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(CorePlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(ImagesPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .build()

    override fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem) {
        markwon.setMarkdown(textView, messageItem.message.text.fixItalicAtEnd())
        Linkify.addLinks(textView)
    }
}
