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

package io.getstream.chat.android.ui.feature.mentions.list.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.ui.feature.mentions.list.MentionListItem
import io.getstream.chat.android.ui.feature.mentions.list.MentionListView.MentionSelectedListener
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle

internal class MentionListAdapter : ListAdapter<MentionListItem, MentionListItemViewHolder<MentionListItem>>(MentionListItemDiffCallback) {

    var mentionSelectedListener: MentionSelectedListener? = null

    var previewStyle: MessagePreviewStyle? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionListItemViewHolder<MentionListItem> = when (viewType) {
        VIEW_TYPE_MESSAGE -> MentionListItemMessageViewHolder(
            style = previewStyle,
            parentView = parent,
            clickListener = mentionSelectedListener,
        )

        VIEW_TYPE_LOADING -> MentionListItemLoadingViewHolder(parent)

        else -> error("Unknown MentionList view type: $viewType")
    } as MentionListItemViewHolder<MentionListItem>

    override fun getItemViewType(position: Int): Int = if (getItem(position) is MentionListItem.MessageItem) {
        VIEW_TYPE_MESSAGE
    } else {
        VIEW_TYPE_LOADING
    }

    override fun onBindViewHolder(holder: MentionListItemViewHolder<MentionListItem>, position: Int) {
        holder.bind(getItem(position))
    }
}

private const val VIEW_TYPE_MESSAGE = 0
private const val VIEW_TYPE_LOADING = 1

private object MentionListItemDiffCallback : DiffUtil.ItemCallback<MentionListItem>() {
    override fun areItemsTheSame(
        oldItem: MentionListItem,
        newItem: MentionListItem,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: MentionListItem,
        newItem: MentionListItem,
    ): Boolean = oldItem == newItem
}
