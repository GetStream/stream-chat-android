/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * Transforms a [MessageListItem.MessageItem] to format or style the [TextView].
 *
 * Instances can be provided by implementing this interface and [installing][ChatUI.messageTextTransformer]
 * that in [ChatUI].
 */
public fun interface ChatMessageTextTransformer {

    /**
     * Transforms a given [MessageListItem.MessageItem] and sets the formatted string to the [TextView].
     *
     * For example, this implementation would convert the message to upper case
     * and then set it to the textView.
     * ```
     * val toUpperCaseTransformer = ChatMessageTextTransformer { textView, messageItem ->
     *      val upperCaseMessage = messageItem.message.text.uppercase(Locale.getDefault())
     *      textView.text = upperCaseMessage
     * }
     */
    public fun transformAndApply(textView: TextView, messageItem: MessageListItem.MessageItem)
}
