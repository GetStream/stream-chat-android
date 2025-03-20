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

package io.getstream.chat.android.ui.feature.channels.list.adapter.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory

internal class ChannelListItemAdapter(
    private val viewHolderFactory: ChannelListItemViewHolderFactory,
) : ListAdapter<ChannelListItem, BaseChannelListItemViewHolder>(ChannelListItemDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChannelListItemViewHolder {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        bind(position, holder, FULL_CHANNEL_LIST_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int, payloads: MutableList<Any>) {
        val diff = (
            payloads
                .filterIsInstance<ChannelListPayloadDiff>()
                .takeIf { it.isNotEmpty() }
                ?: listOf(FULL_CHANNEL_LIST_ITEM_PAYLOAD_DIFF)
            ).fold(EMPTY_CHANNEL_LIST_ITEM_PAYLOAD_DIFF, ChannelListPayloadDiff::plus)

        bind(position, holder, diff)
    }

    private fun bind(position: Int, holder: BaseChannelListItemViewHolder, payload: ChannelListPayloadDiff) {
        when (val channelItem = getItem(position)) {
            is ChannelListItem.LoadingMoreItem -> Unit
            is ChannelListItem.ChannelItem -> holder.bind(channelItem, payload)
        }
    }

    internal fun getChannel(cid: String): Channel {
        return currentList
            .asSequence()
            .filterIsInstance<ChannelListItem.ChannelItem>()
            .first { it.channel.cid == cid }
            .channel
    }

    companion object {
        private val FULL_CHANNEL_LIST_ITEM_PAYLOAD_DIFF: ChannelListPayloadDiff = ChannelListPayloadDiff(
            nameChanged = true,
            avatarViewChanged = true,
            usersChanged = true,
            lastMessageChanged = true,
            readStateChanged = true,
            unreadCountChanged = true,
            extraDataChanged = true,
            typingUsersChanged = true,
            draftMessageChanged = true,
        )

        val EMPTY_CHANNEL_LIST_ITEM_PAYLOAD_DIFF: ChannelListPayloadDiff = ChannelListPayloadDiff(
            nameChanged = false,
            avatarViewChanged = false,
            usersChanged = false,
            lastMessageChanged = false,
            readStateChanged = false,
            unreadCountChanged = false,
            extraDataChanged = false,
            typingUsersChanged = false,
            draftMessageChanged = false,
        )
    }
}
