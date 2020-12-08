package com.getstream.sdk.chat.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.view.channels.ChannelListView
import com.getstream.sdk.chat.view.channels.ChannelListViewStyle
import io.getstream.chat.android.client.models.Channel

public abstract class BaseChannelListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public abstract fun bind(channelState: Channel, position: Int, payloads: ChannelItemPayloadDiff)
    public abstract fun setStyle(style: ChannelListViewStyle)
    public abstract fun setUserClickListener(userClickListener: ChannelListView.UserClickListener?)
    public abstract fun setChannelClickListener(channelClickListener: ChannelListView.ChannelClickListener?)
    public abstract fun setChannelLongClickListener(channelClickListener: ChannelListView.ChannelClickListener?)
}
