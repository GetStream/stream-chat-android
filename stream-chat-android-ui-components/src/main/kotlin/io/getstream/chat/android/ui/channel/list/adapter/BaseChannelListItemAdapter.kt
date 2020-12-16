package io.getstream.chat.android.ui.channel.list.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.diff
import io.getstream.chat.android.ui.utils.extensions.safeCast

public abstract class BaseChannelListItemAdapter :
    ListAdapter<ChannelListItem, BaseChannelListItemViewHolder>(DIFF_CALLBACK) {

    public open var style: ChannelListViewStyle? = null

    public companion object {
        public val DIFF_CALLBACK: DiffUtil.ItemCallback<ChannelListItem> =
            object : DiffUtil.ItemCallback<ChannelListItem>() {
                override fun areItemsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem): Boolean {
                    if (oldItem::class != newItem::class) {
                        return false
                    }

                    return when (oldItem) {
                        is ChannelListItem.ChannelItem -> oldItem.channel.cid == newItem.safeCast<ChannelListItem.ChannelItem>()?.channel?.cid
                        else -> true
                    }
                }

                override fun areContentsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem): Boolean {
                    // this is only called if areItemsTheSame returns true, so they must be the same class
                    return when (oldItem) {
                        is ChannelListItem.ChannelItem -> {
                            oldItem
                                .channel
                                .diff(newItem.cast<ChannelListItem.ChannelItem>().channel)
                                .hasDifference()
                                .not()
                        }
                        else -> true
                    }
                }

                override fun getChangePayload(oldItem: ChannelListItem, newItem: ChannelListItem): Any {
                    // only called if their contents aren't the same, so they must be channel items and not loading items
                    return oldItem
                        .cast<ChannelListItem.ChannelItem>()
                        .channel
                        .diff(newItem.cast<ChannelListItem.ChannelItem>().channel)
                }
            }
    }
}
