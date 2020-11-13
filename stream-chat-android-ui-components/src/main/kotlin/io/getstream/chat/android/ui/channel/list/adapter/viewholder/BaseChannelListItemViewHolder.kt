package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListViewClickDelegate
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelItemDiff

public abstract class BaseChannelListItemViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView),
    ChannelListViewClickDelegate {

    public var style: ChannelListViewStyle? = null

    public override var userClickListener: ChannelListView.UserClickListener? = null

    public override var channelClickListener: ChannelListView.ChannelClickListener? = null

    public override var channelLongClickListener: ChannelListView.ChannelClickListener? = null

    public abstract fun bind(channel: Channel, position: Int, diff: ChannelItemDiff? = null)
}
