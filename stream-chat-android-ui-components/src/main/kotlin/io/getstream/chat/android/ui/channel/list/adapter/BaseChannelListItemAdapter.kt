package io.getstream.chat.android.ui.channel.list.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.utils.extensions.diff

public abstract class BaseChannelListItemAdapter :
    ListAdapter<Channel, BaseChannelListItemViewHolder>(DIFF_CALLBACK) {

    public open var style: ChannelListViewStyle? = null

    public open var channelClickListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener {}

    public open var channelLongClickListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener {}

    public open var deleteClickListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener {}

    public open var userClickListener: ChannelListView.UserClickListener =
        ChannelListView.UserClickListener {}

    public companion object {
        public val DIFF_CALLBACK: DiffUtil.ItemCallback<Channel> = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean = oldItem.cid == newItem.cid

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean =
                !oldItem.diff(newItem).hasDifference()

            override fun getChangePayload(oldItem: Channel, newItem: Channel): Any? = oldItem.diff(newItem)
        }
    }
}
