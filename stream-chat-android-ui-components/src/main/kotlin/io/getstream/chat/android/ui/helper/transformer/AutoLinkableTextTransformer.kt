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

package io.getstream.chat.android.ui.helper.transformer

import android.widget.TextView
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.utils.Linkify

/**
 * AutoLinkable implementation of [ChatMessageTextTransformer] that makes [TextView] links clickable after applying the transformer.
 *
 * By default our SDK text views don't have `android:autoLink` set due to a limitation in Markdown linkify implementation.
 */
public class AutoLinkableTextTransformer(
    public val transformer: (textView: TextView, messageItem: MessageListItem.MessageItem) -> Unit,
) : ChatMessageTextTransformer {

    override fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem) {
        transformer(textView, messageItem)
        Linkify.addLinks(textView)
    }
}
