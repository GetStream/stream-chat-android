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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.feature.internal.MessageResultDiffCallback
import io.getstream.chat.android.ui.feature.search.internal.SearchResultListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView.SearchResultSelectedListener
import io.getstream.chat.android.ui.feature.search.list.SearchResultListViewStyle
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class SearchResultListAdapter(
    private val style: SearchResultListViewStyle,
) : ListAdapter<MessageResult, MessagePreviewViewHolder>(MessageResultDiffCallback) {

    private var searchResultSelectedListener: SearchResultSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder = StreamUiItemMentionListBinding
        .inflate(parent.streamThemeInflater, parent, false)
        .let { MessagePreviewViewHolder(it, style) }

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

        internal fun bind(messageResult: MessageResult) {
            this.message = messageResult.message
            binding.root.renderMessageResult(messageResult)
        }
    }
}
