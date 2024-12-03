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

package io.getstream.chat.android.ui.feature.pinned.list.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.databinding.StreamUiPinnedMessageListLoadingMoreViewBinding
import io.getstream.chat.android.ui.feature.internal.MessageResultDiffCallback
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.feature.pinned.list.PinnedMessageListView.PinnedMessageSelectedListener
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class PinnedMessageListAdapter : ListAdapter<MessageResult, RecyclerView.ViewHolder>(MessageResultDiffCallback) {

    private var pinnedMessageSelectedListener: PinnedMessageSelectedListener? = null

    var messagePreviewStyle: MessagePreviewStyle? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_MESSAGE) {
            StreamUiItemMentionListBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let { binding ->
                    messagePreviewStyle?.let(binding.root::styleView)
                    MessagePreviewViewHolder(binding)
                }
        } else {
            StreamUiPinnedMessageListLoadingMoreViewBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let { binding ->
                    PinnedMessageLoadingMoreView(binding)
                }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessagePreviewViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).message.id.isNotEmpty()) {
            ITEM_MESSAGE
        } else {
            ITEM_LOADING_MORE
        }
    }

    fun setPinnedMessageSelectedListener(pinnedMessageSelectedListener: PinnedMessageSelectedListener?) {
        this.pinnedMessageSelectedListener = pinnedMessageSelectedListener
    }

    companion object {
        private const val ITEM_MESSAGE = 0
        private const val ITEM_LOADING_MORE = 1
    }

    inner class PinnedMessageLoadingMoreView(
        binding: StreamUiPinnedMessageListLoadingMoreViewBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    inner class MessagePreviewViewHolder(
        private val binding: StreamUiItemMentionListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var message: Message

        init {
            binding.root.setOnClickListener {
                pinnedMessageSelectedListener?.onPinnedMessageSelected(message)
            }
        }

        internal fun bind(messageResult: MessageResult) {
            this.message = messageResult.message
            binding.root.renderMessageResult(messageResult)
        }
    }
}
