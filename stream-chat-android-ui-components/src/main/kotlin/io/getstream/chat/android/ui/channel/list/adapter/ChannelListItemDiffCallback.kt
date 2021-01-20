package io.getstream.chat.android.ui.channel.list.adapter

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.diff
import io.getstream.chat.android.ui.utils.extensions.safeCast

internal object ChannelListItemDiffCallback : DiffUtil.ItemCallback<ChannelListItem>() {
    override fun areItemsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }

        return when (oldItem) {
            is ChannelListItem.ChannelItem -> {
                oldItem.channel.cid == newItem.safeCast<ChannelListItem.ChannelItem>()?.channel?.cid
            }

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
