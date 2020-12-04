package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import io.getstream.chat.android.ui.R

public class ChannelViewHolderFactory :
    BaseChannelViewHolderFactory<ChannelListItemViewHolder>(R.layout.stream_ui_channel_list_item_view) {

    override fun createChannelViewHolder(itemView: View): ChannelListItemViewHolder =
        ChannelListItemViewHolder(itemView)
}
