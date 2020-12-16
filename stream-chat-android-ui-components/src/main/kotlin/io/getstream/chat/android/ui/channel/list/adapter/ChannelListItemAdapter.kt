package io.getstream.chat.android.ui.channel.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListListenerProvider
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.firstOrDefault

internal class ChannelListItemAdapter : BaseChannelListItemAdapter() {

    var viewHolderFactory: ChannelListItemViewHolderFactory = ChannelListItemViewHolderFactory()

    val listenerProvider: ChannelListListenerProvider = ChannelListListenerProvider()

    companion object {
        val EVERYTHING_CHANGED: ChannelDiff = ChannelDiff()
        val NOTHING_CHANGED: ChannelDiff = ChannelDiff(
            nameChanged = false,
            avatarViewChanged = false,
            lastMessageChanged = false,
            readStateChanged = false
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChannelListItem.LoadingMoreItem -> ChannelItemType.LOADING_MORE.ordinal
            is ChannelListItem.ChannelItem -> ChannelItemType.DEFAULT.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChannelListItemViewHolder {
        return with(listenerProvider) {
            viewHolderFactory.createViewHolder(
                parent,
                ChannelItemType.values()[viewType],
                channelClickListener,
                channelLongClickListener,
                deleteClickListener,
                moreOptionsClickListener,
                userClickListener,
                swipeListener,
                style
            )
        }
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

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int, payloads: MutableList<Any>) {
        bind(position, holder, payloads.firstOrDefault(EVERYTHING_CHANGED).cast())
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        bind(position, holder, NOTHING_CHANGED)
    }
}
