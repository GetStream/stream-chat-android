package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import io.getstream.chat.android.ui.channel.list.ChannelListView

public interface ChannelListListenerContainer {
    public val channelClickListener: ChannelListView.ChannelClickListener
    public val channelLongClickListener: ChannelListView.ChannelLongClickListener
    public val deleteClickListener: ChannelListView.ChannelClickListener
    public val moreOptionsClickListener: ChannelListView.ChannelClickListener
    public val userClickListener: ChannelListView.UserClickListener
    public val swipeListener: ChannelListView.SwipeListener
}
