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

package io.getstream.chat.android.ui.feature.mentions.list.internal

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.databinding.StreamUiItemListLoadingBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.feature.mentions.list.MentionListItem
import io.getstream.chat.android.ui.feature.mentions.list.MentionListView.MentionSelectedListener
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal abstract class MentionListItemViewHolder<Item : MentionListItem>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: Item)
}

internal class MentionListItemMessageViewHolder(
    style: MessagePreviewStyle?,
    parentView: ViewGroup,
    private val binding: StreamUiItemMentionListBinding = StreamUiItemMentionListBinding.inflate(
        parentView.streamThemeInflater,
        parentView,
        false,
    ),
    private val clickListener: MentionSelectedListener?,
) : MentionListItemViewHolder<MentionListItem.MessageItem>(binding.root) {

    private lateinit var message: Message

    init {
        style?.let(binding.root::styleView)
        itemView.setOnClickListener { clickListener?.onMentionSelected(message) }
    }

    override fun bind(item: MentionListItem.MessageItem) {
        message = item.message.message
        binding.root.renderMessageResult(item.message)
    }
}

internal class MentionListItemLoadingViewHolder(
    private val parentView: ViewGroup,
    binding: StreamUiItemListLoadingBinding = StreamUiItemListLoadingBinding.inflate(
        parentView.streamThemeInflater,
        parentView,
        false,
    ),
) : MentionListItemViewHolder<MentionListItem.LoadingItem>(binding.root) {
    override fun bind(item: MentionListItem.LoadingItem) {
        // no-op
    }
}
