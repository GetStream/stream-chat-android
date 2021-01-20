package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff

public abstract class BaseChannelListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public abstract fun bind(channel: Channel, diff: ChannelListPayloadDiff)
}
