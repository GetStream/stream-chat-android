package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.databinding.StreamChannelListLoadingViewBinding
import io.getstream.chat.android.ui.utils.extensions.inflater

public class LoadingMoreViewHolder(
    parent: ViewGroup,
    binding: StreamChannelListLoadingViewBinding = StreamChannelListLoadingViewBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseChannelListItemViewHolder(binding.root) {

    override fun bind(channel: Channel, diff: ChannelDiff) {
        // no-op
    }
}
