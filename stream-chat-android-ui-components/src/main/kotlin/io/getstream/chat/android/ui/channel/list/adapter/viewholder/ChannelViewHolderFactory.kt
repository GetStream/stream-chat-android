package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemAdapter

public class ChannelViewHolderFactory : BaseChannelViewHolderFactory() {

    public override fun createChannelViewHolder(
        parent: ViewGroup,
        channelItemType: ChannelListItemAdapter.ChannelItemType,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        deleteClickListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        style: ChannelListViewStyle?
    ): BaseChannelListItemViewHolder = when (channelItemType) {

        ChannelListItemAdapter.ChannelItemType.LOADING_MORE -> LoadingViewHolder(parent)

        else -> ChannelListItemViewHolder(
            parent,
            channelClickListener,
            channelLongClickListener,
            deleteClickListener,
            userClickListener,
            style
        )
    }
}
