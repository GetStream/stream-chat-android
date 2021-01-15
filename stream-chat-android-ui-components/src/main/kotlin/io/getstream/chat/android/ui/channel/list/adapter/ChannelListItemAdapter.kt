package io.getstream.chat.android.ui.channel.list.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.diff
import io.getstream.chat.android.ui.utils.extensions.firstOrDefault
import io.getstream.chat.android.ui.utils.extensions.safeCast

internal class ChannelListItemAdapter(
    private val viewHolderFactory: ChannelListItemViewHolderFactory,
) : ListAdapter<ChannelListItem, BaseChannelListItemViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ChannelListItem> =
            object : DiffUtil.ItemCallback<ChannelListItem>() {
                override fun areItemsTheSame(
                    oldItem: ChannelListItem,
                    newItem: ChannelListItem
                ): Boolean {
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

                override fun areContentsTheSame(
                    oldItem: ChannelListItem,
                    newItem: ChannelListItem
                ): Boolean {
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

                override fun getChangePayload(
                    oldItem: ChannelListItem,
                    newItem: ChannelListItem
                ): Any {
                    // only called if their contents aren't the same, so they must be channel items and not loading items
                    return oldItem
                        .cast<ChannelListItem.ChannelItem>()
                        .channel
                        .diff(newItem.cast<ChannelListItem.ChannelItem>().channel)
                }
            }

        val EVERYTHING_CHANGED: ChannelDiff = ChannelDiff()
        val NOTHING_CHANGED: ChannelDiff = ChannelDiff(
            nameChanged = false,
            avatarViewChanged = false,
            lastMessageChanged = false,
            readStateChanged = false
        )
    }

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseChannelListItemViewHolder {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    private fun bind(
        position: Int,
        holder: BaseChannelListItemViewHolder,
        payload: ChannelDiff
    ) {
        when (val channelItem = getItem(position)) {
            is ChannelListItem.LoadingMoreItem -> Unit
            is ChannelListItem.ChannelItem -> holder.bind(channelItem.channel, payload)
        }
    }

    override fun onBindViewHolder(
        holder: BaseChannelListItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        bind(position, holder, payloads.firstOrDefault(EVERYTHING_CHANGED).cast())
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        bind(position, holder, NOTHING_CHANGED)
    }

    internal fun getChannel(cid: String): Channel {
        return currentList
            .asSequence()
            .filterIsInstance<ChannelListItem.ChannelItem>()
            .first { it.channel.cid == cid }
            .channel
    }
}
