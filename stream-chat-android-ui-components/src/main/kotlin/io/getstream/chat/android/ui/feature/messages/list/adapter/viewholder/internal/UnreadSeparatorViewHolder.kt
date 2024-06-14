/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemUnreadSeparatorBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class UnreadSeparatorViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val messageListListeners: MessageListListeners?,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemUnreadSeparatorBinding = StreamUiItemUnreadSeparatorBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.UnreadSeparatorItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.UnreadSeparatorItem, diff: MessageListItemPayloadDiff) {
        super.bindData(data, diff)

        binding.root.setBackgroundColor(style.unreadSeparatorBackgroundColor)
        binding.unreadSeparatorLabel.setTextStyle(style.unreadSeparatorTextStyle)
        binding.unreadSeparatorLabel.text =
            context.resources.getString(R.string.stream_ui_message_list_unread_separator)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        messageListListeners?.unreadLabelReachedListener?.onUnreadLabelReached()
    }
}
