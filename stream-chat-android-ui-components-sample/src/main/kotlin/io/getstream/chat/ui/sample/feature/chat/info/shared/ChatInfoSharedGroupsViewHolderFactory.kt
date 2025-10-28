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

package io.getstream.chat.ui.sample.feature.chat.info.shared

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedGroupsItemBinding

class ChatInfoSharedGroupsViewHolderFactory : ChannelListItemViewHolderFactory() {

    override fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder = ChatInfoSharedGroupsViewHolder(parentView, listenerContainer.channelClickListener)
}

class ChatInfoSharedGroupsViewHolder(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val binding: ChatInfoSharedGroupsItemBinding = ChatInfoSharedGroupsItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
    ),
) : BaseChannelListItemViewHolder(binding.root) {

    private lateinit var channel: Channel

    init {
        binding.root.setOnClickListener { channelClickListener.onClick(channel) }
    }

    override fun bind(channelItem: ChannelListItem.ChannelItem, diff: ChannelListPayloadDiff) {
        this.channel = channelItem.channel

        binding.apply {
            channelAvatarView.setChannel(channel)
            nameTextView.text = ChatUI.channelNameFormatter.formatChannelName(
                channel = channel,
                currentUser = ChatClient.instance().clientState.user.value,
            )
            membersCountTextView.text = itemView.context.resources.getQuantityString(
                R.plurals.members_count_title,
                channel.members.size,
                channel.members.size,
            )
        }
    }
}
