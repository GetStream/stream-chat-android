package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes

public class ChannelViewHolderFactory : BaseChannelViewHolderFactory<ChannelListItemViewHolder> {
    public override fun createChannelViewHolder(
        @LayoutRes layout: Int,
        parent: ViewGroup,
        viewType: Int
    ): ChannelListItemViewHolder = parent.context
        .let(LayoutInflater::from)
        .inflate(layout, parent, false)
        .let(::ChannelListItemViewHolder)
}
