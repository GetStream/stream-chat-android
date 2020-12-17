package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.databinding.StreamChannelListLoadingViewBinding

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
