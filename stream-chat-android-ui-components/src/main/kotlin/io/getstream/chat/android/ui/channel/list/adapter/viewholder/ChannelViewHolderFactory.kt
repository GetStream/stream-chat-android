package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle

public class ChannelViewHolderFactory(
    public override var channelClickListener: ChannelListView.ChannelClickListener? = null,
    public override var channelLongClickListener: ChannelListView.ChannelClickListener? = null,
    public override var userClickListener: ChannelListView.UserClickListener? = null,
    public override var style: ChannelListViewStyle? = null,
) : BaseChannelViewHolderFactory<ChannelListItemViewHolder>(R.layout.stream_channel_list_item_view) {

    override fun createChannelViewHolder(itemView: View): ChannelListItemViewHolder =
        ChannelListItemViewHolder(
            itemView,
            channelClickListener,
            channelLongClickListener,
            userClickListener,
            style
        )
}
