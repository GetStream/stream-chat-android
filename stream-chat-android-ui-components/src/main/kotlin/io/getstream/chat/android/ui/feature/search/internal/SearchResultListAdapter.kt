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

package io.getstream.chat.android.ui.feature.search.internal

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.feature.search.internal.SearchResultListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView.SearchResultSelectedListener
import io.getstream.chat.android.ui.feature.search.list.SearchResultListViewStyle
import io.getstream.chat.android.ui.utils.extensions.asMention
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class SearchResultListAdapter(
    private val style: SearchResultListViewStyle,
) : ListAdapter<Message, MessagePreviewViewHolder>(MessageDiffCallback) {

    private var searchResultSelectedListener: SearchResultSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        return StreamUiItemMentionListBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { MessagePreviewViewHolder(it, style) }
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSearchResultSelectedListener(searchResultSelectedListener: SearchResultSelectedListener?) {
        this.searchResultSelectedListener = searchResultSelectedListener
    }

    inner class MessagePreviewViewHolder(
        private val binding: StreamUiItemMentionListBinding,
        private val style: SearchResultListViewStyle,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var message: Message

        init {
            binding.root.setOnClickListener {
                searchResultSelectedListener?.onSearchResultSelected(message)
            }
            binding.root.styleView(style.messagePreviewStyle)
            binding.root.binding.apply {
                contentRoot.updateLayoutParams {
                    height = style.itemHeight
                }
                userAvatarView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = style.itemMarginStart
                }
                senderNameLabel.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = style.itemTitleMarginStart
                    marginEnd = style.itemMarginEnd
                }
                messageTimeLabel.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginEnd = style.itemMarginEnd
                }
                spacer.updateLayoutParams {
                    height = style.itemVerticalSpacerHeight
                }
                guideline.setGuidelinePercent(style.itemVerticalSpacerPosition)
            }
        }

        internal fun bind(message: Message) {
            this.message = message
            binding.root.setMessage(message, ChatUI.currentUserProvider.getCurrentUser()?.asMention(context))
        }
    }

    private object MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Comparing only properties used by the ViewHolder
            return oldItem.id == newItem.id &&
                oldItem.createdAt == newItem.createdAt &&
                oldItem.createdLocallyAt == newItem.createdLocallyAt &&
                oldItem.text == newItem.text &&
                oldItem.user == newItem.user
        }
    }
}
