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

package io.getstream.chat.android.ui.feature.messages.list.reactions.internal

import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageReactionBinding
import io.getstream.chat.android.ui.feature.messages.list.reactions.ReactionClickListener
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class ReactionsAdapter(
    @Px private val itemSize: Int,
    private val reactionClickListener: ReactionClickListener,
) : ListAdapter<ReactionItem, ReactionsAdapter.ReactionViewHolder>(ReactionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder = StreamUiItemMessageReactionBinding
        .inflate(parent.streamThemeInflater, parent, false)
        .let { ReactionViewHolder(it, itemSize, reactionClickListener) }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReactionViewHolder(
        private val binding: StreamUiItemMessageReactionBinding,
        @Px private val itemSize: Int,
        private val reactionClickListener: ReactionClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var reactionItem: ReactionItem

        init {
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                width = itemSize
                height = itemSize
            }
            binding.root.setOnClickListener {
                reactionClickListener.onReactionClick(reactionItem.type)
            }
        }

        fun bind(reactionItem: ReactionItem) {
            this.reactionItem = reactionItem
            binding.reactionIcon.setImageDrawable(reactionItem.drawable)
        }
    }
}
