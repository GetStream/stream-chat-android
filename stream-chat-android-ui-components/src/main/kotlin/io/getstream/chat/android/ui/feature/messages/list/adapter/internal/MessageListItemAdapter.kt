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

package io.getstream.chat.android.ui.feature.messages.list.adapter.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.extensions.internal.doForAllViewHolders
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewHolderFactory

internal class MessageListItemAdapter(
    private val viewHolderFactory: MessageListItemViewHolderFactory,
) : ListAdapter<MessageListItem, BaseMessageItemViewHolder<out MessageListItem>>(MessageListItemDiffCallback) {

    var isThread: Boolean = false

    override fun getItemId(position: Int): Long = getItem(position).getStableId()

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseMessageItemViewHolder<out MessageListItem>, position: Int) {
        holder.bindListItem(getItem(position), FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(
        holder: BaseMessageItemViewHolder<out MessageListItem>,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        val diff = (
            payloads
                .filterIsInstance<MessageListItemPayloadDiff>()
                .takeIf { it.isNotEmpty() }
                ?: listOf(FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF)
            )
            .fold(EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF) { acc, messageListItemPayloadDiff ->
                acc + messageListItemPayloadDiff
            }

        holder.bindListItem(getItem(position), diff)
    }

    override fun onViewRecycled(holder: BaseMessageItemViewHolder<out MessageListItem>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun onViewAttachedToWindow(holder: BaseMessageItemViewHolder<out MessageListItem>) {
        super.onViewAttachedToWindow(holder)
        holder.onAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: BaseMessageItemViewHolder<out MessageListItem>) {
        holder.onDetachedFromWindow()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        doForAllViewHolders(recyclerView) { it.onAttachedToWindow() }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        doForAllViewHolders(recyclerView) { it.onDetachedFromWindow() }
        super.onDetachedFromRecyclerView(recyclerView)
    }

    companion object {
        private val FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = true,
            replyText = true,
            reactions = true,
            attachments = true,
            replies = true,
            syncStatus = true,
            deleted = true,
            positions = true,
            pinned = true,
            user = true,
            mentions = true,
            footer = true,
        )
        private val EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = false,
            replyText = false,
            reactions = false,
            attachments = false,
            replies = false,
            syncStatus = false,
            deleted = false,
            positions = false,
            pinned = false,
            user = false,
            mentions = false,
            footer = false,
        )
    }
}
