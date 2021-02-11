package io.getstream.chat.android.ui.channel.list.adapter.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory

internal class ChannelListItemAdapter(
    private val viewHolderFactory: ChannelListItemViewHolderFactory,
) : ListAdapter<ChannelListItem, BaseChannelListItemViewHolder>(ChannelListItemDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChannelListItemViewHolder {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        bind(position, holder, FULL_CHANNEL_LIST_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int, payloads: MutableList<Any>) {
        val diff = (
            payloads
                .filterIsInstance<ChannelListPayloadDiff>()
                .takeIf { it.isNotEmpty() }
                ?: listOf(FULL_CHANNEL_LIST_ITEM_PAYLOAD_DIFF)
            )
            .fold(EMPTY_CHANNEL_LIST_ITEM_PAYLOAD_DIFF) { acc, channelListItemPayloadDiff ->
                acc + channelListItemPayloadDiff
            }

        bind(position, holder, diff)
    }

    private fun bind(position: Int, holder: BaseChannelListItemViewHolder, payload: ChannelListPayloadDiff) {
        when (val channelItem = getItem(position)) {
            is ChannelListItem.LoadingMoreItem -> Unit
            is ChannelListItem.ChannelItem -> holder.bind(channelItem.channel, payload)
        }
    }

    internal fun getChannel(cid: String): Channel {
        return currentList
            .asSequence()
            .filterIsInstance<ChannelListItem.ChannelItem>()
            .first { it.channel.cid == cid }
            .channel
    }

    companion object {
        private val FULL_CHANNEL_LIST_ITEM_PAYLOAD_DIFF: ChannelListPayloadDiff = ChannelListPayloadDiff(
            nameChanged = true,
            avatarViewChanged = true,
            lastMessageChanged = true,
            readStateChanged = true,
            unreadCountChanged = true,
        )

        val EMPTY_CHANNEL_LIST_ITEM_PAYLOAD_DIFF: ChannelListPayloadDiff = ChannelListPayloadDiff(
            nameChanged = false,
            avatarViewChanged = false,
            lastMessageChanged = false,
            readStateChanged = false,
            unreadCountChanged = false,
        )
    }
}
