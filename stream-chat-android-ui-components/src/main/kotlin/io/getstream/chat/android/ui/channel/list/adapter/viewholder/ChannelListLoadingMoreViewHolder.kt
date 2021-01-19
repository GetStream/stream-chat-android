package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.databinding.StreamUiChannelListLoadingViewBinding

internal class ChannelListLoadingMoreViewHolder(
    parent: ViewGroup,
    binding: StreamUiChannelListLoadingViewBinding = StreamUiChannelListLoadingViewBinding.inflate(
        parent.inflater,
        parent,
        false
    ),
) : BaseChannelListItemViewHolder(binding.root) {

    override fun bind(channel: Channel, diff: ChannelListPayloadDiff): Unit = Unit
}
