package io.getstream.chat.android.ui.channel.list.adapter

import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.utils.extensions.DIFF_CALLBACK

public abstract class BaseChannelListItemAdapter :
    ListAdapter<Channel, BaseChannelListItemViewHolder>(Channel.DIFF_CALLBACK) {

    public open var style: ChannelListViewStyle? = null
    public open var channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener {}
    public open var channelLongClickListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener {}
    public open var userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener {}
}
