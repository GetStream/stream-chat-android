package io.getstream.chat.android.ui.channel.list.adapter

import io.getstream.chat.android.ui.channel.list.ChannelListView

public interface ChannelListViewClickDelegate {
    public val userClickListener: ChannelListView.UserClickListener?

    public val channelClickListener: ChannelListView.ChannelClickListener?

    public val channelLongClickListener: ChannelListView.ChannelClickListener?
}
