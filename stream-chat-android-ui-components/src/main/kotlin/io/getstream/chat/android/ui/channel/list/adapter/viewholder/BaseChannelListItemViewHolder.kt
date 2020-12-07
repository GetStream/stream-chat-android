package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff

public abstract class BaseChannelListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    protected open val channelClickListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener.DEFAULT

    protected open val channelLongClickListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener.DEFAULT

    protected open val channelDeleteListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener.DEFAULT

    protected open val userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT

    protected open val style: ChannelListViewStyle? = null

    public abstract fun bind(channel: Channel, diff: ChannelDiff)
}
