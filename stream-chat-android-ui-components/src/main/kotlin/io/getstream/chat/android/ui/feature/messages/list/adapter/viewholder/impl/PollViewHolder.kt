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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl

import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePollBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

public class PollViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val messageListListeners: MessageListListeners?,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemMessagePollBinding = StreamUiItemMessagePollBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff) {
        super.bindData(data, diff)
        data.message.poll
            ?.takeIf { diff.poll }
            ?.let { poll ->
                binding.pollView.setPoll(poll, data.isMine)
                binding.pollView.onOptionClick = { option ->
                    messageListListeners?.onPollOptionClickListener?.onPollOptionClick(data.message, poll, option)
                }
                binding.pollView.onClosePollClick = {
                    messageListListeners?.onPollCloseClickListener?.onPollCloseClick(poll)
                }
                binding.pollView.onViewPollResultsClick = {
                    messageListListeners?.onViewPollResultClickListener?.onViewPollResultClick(poll)
                }
                binding.pollView.onShowAllPollOptionClick = {
                    messageListListeners?.onShowAllPollOptionClickListener?.onShowAllPollOptionClick(data.message, poll)
                }
            }
    }
}
