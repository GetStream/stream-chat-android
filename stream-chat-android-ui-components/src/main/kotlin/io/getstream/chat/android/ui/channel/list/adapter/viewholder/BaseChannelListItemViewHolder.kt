package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff

public abstract class BaseChannelListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    public abstract val channelClickListener: ChannelListView.ChannelClickListener
    public abstract val channelLongClickListener: ChannelListView.ChannelClickListener
    public abstract val channelDeleteListener: ChannelListView.ChannelClickListener
    public abstract val userClickListener: ChannelListView.UserClickListener

    protected open val style: ChannelListViewStyle? = null

    public abstract fun bind(channel: Channel, diff: ChannelDiff)
}
