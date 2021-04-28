package io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiChannelListLoadingViewBinding

internal class ChannelListLoadingMoreViewHolder(
    parent: ViewGroup,
    binding: StreamUiChannelListLoadingViewBinding = StreamUiChannelListLoadingViewBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : BaseChannelListItemViewHolder(binding.root) {

    override fun bind(channel: Channel, diff: ChannelListPayloadDiff): Unit = Unit
}
