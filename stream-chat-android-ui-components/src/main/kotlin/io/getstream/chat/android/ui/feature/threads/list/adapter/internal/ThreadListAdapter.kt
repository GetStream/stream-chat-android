/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.threads.list.adapter.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.ui.feature.threads.list.ThreadListViewStyle
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItem
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItemViewHolderFactory
import io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.BaseThreadListItemViewHolder
import io.getstream.log.taggedLogger

/**
 * RecyclerView adapter implementation for displaying a list of threads.
 *
 * @param style The [ThreadListViewStyle] for item customization.
 * @param viewHolderFactory The factory for creating view holders.
 */
internal class ThreadListAdapter(
    private val style: ThreadListViewStyle,
    private val viewHolderFactory: ThreadListItemViewHolderFactory,
) : ListAdapter<ThreadListItem, BaseThreadListItemViewHolder<out ThreadListItem>>(ThreadListItemDiffCallback) {

    private val logger by taggedLogger("Chat:ThreadListAdapter")

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).stableId
    }

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseThreadListItemViewHolder<out ThreadListItem> {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseThreadListItemViewHolder<out ThreadListItem>, position: Int) {
        val item = getItem(position)
        val itemViewType = viewHolderFactory.getItemViewType(item)
        val holderViewType = viewHolderFactory.getItemViewType(holder)
        if (itemViewType != holderViewType) {
            // Should never happen
            logger.d { "Item view type $itemViewType does not match the holder view type $holderViewType" }
            return
        }
        holder.bindInternal(item)
    }

    /**
     * [DiffUtil.ItemCallback] for calculating differences between [ThreadListItem]s.
     */
    private object ThreadListItemDiffCallback : DiffUtil.ItemCallback<ThreadListItem>() {
        override fun areItemsTheSame(oldItem: ThreadListItem, newItem: ThreadListItem): Boolean {
            return oldItem.stableId == newItem.stableId
        }

        override fun areContentsTheSame(oldItem: ThreadListItem, newItem: ThreadListItem): Boolean {
            return if (oldItem is ThreadListItem.ThreadItem && newItem is ThreadListItem.ThreadItem) {
                oldItem.thread == newItem.thread // [Thread] is a data class, equality check is enough
            } else {
                false
            }
        }
    }
}
