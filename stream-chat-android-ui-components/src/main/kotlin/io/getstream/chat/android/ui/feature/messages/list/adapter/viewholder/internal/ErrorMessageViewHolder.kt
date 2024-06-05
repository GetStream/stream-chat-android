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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal

import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiItemErrorMessageBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.getTranslatedText
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class ErrorMessageViewHolder(
    parent: ViewGroup,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemErrorMessageBinding = StreamUiItemErrorMessageBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff) {
        if (!diff.syncStatus) return

        val displayedText = data.message.getTranslatedText()
        binding.messageTextView.text = displayedText
        binding.messageTextView.setTextStyle(style.textStyleErrorMessage)
    }
}
