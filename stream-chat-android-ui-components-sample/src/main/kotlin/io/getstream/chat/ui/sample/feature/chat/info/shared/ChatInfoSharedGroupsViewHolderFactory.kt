package io.getstream.chat.ui.sample.feature.chat.info.shared

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedGroupsItemBinding

class ChatInfoSharedGroupsViewHolderFactory : ChannelListItemViewHolderFactory() {

    override fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return ChatInfoSharedGroupsViewHolder(parentView, listenerContainer.channelClickListener)
    }
}

class ChatInfoSharedGroupsViewHolder(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val binding: ChatInfoSharedGroupsItemBinding = ChatInfoSharedGroupsItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    ),
) : BaseChannelListItemViewHolder(binding.root) {

    private lateinit var channel: Channel

    init {
        binding.root.setOnClickListener { channelClickListener.onClick(channel) }
    }

    override fun bind(channel: Channel, diff: ChannelListPayloadDiff) {
        this.channel = channel

        binding.apply {
            avatarView.setChannelData(channel)
            nameTextView.text = channel.getDisplayName(itemView.context)
            membersCountTextView.text = itemView.context.resources.getQuantityString(
                R.plurals.members_count_title,
                channel.members.size,
                channel.members.size
            )
        }
    }
}
