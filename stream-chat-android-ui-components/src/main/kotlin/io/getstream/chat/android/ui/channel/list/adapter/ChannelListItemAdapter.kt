package io.getstream.chat.android.ui.channel.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemListenerContainer
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.firstOrDefault

public class ChannelListItemAdapter : BaseChannelListItemAdapter() {

    public var viewHolderFactory: BaseChannelViewHolderFactory = ChannelViewHolderFactory()

    public var listenerContainer: ChannelListItemListenerContainer = ChannelListItemListenerContainer()

    public var endReached: Boolean = true
        set(value) {
            field = value
            if (!value) {
                // if we've reached the end, remove the last item
                notifyItemRemoved(itemCount)
            }
        }

    public companion object {
        public val EVERYTHING_CHANGED: ChannelDiff = ChannelDiff()
        public val NOTHING_CHANGED: ChannelDiff = ChannelDiff(
            nameChanged = false,
            avatarViewChanged = false,
            lastMessageChanged = false,
            readStateChanged = false
        )
    }

    public enum class ChannelItemType {
        DEFAULT,
        LOADING_MORE
    }

    // If we haven't reached the end of the channels, and we're in the last position, we're loading more
    private fun isLoadingMore(position: Int) = !endReached && position == itemCount - 1

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoadingMore(position) -> ChannelItemType.LOADING_MORE.ordinal
            else -> ChannelItemType.DEFAULT.ordinal
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount().let { realCount ->
            when {
                // if the list isn't empty, and we haven't reached the end, always offset +1 for the loading more view
                !endReached && realCount > 0 -> realCount + 1
                else -> realCount
            }
        }
    }

    override fun getItem(position: Int): Channel? {
        return when {
            // don't try to fetch an item that isn't in the data set
            isLoadingMore(position) -> null
            else -> super.getItem(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChannelListItemViewHolder {
        return with(listenerContainer) {
            viewHolderFactory.createChannelViewHolder(
                parent,
                viewType,
                channelClickListener,
                channelLongClickListener,
                deleteClickListener,
                userClickListener,
                style
            )
        }
    }

    /* Loading view doesn't require any binding. Only bind if a channel item is retrieved */
    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int, payloads: MutableList<Any>) {
        getItem(position)?.let { holder.bind(it, payloads.firstOrDefault(EVERYTHING_CHANGED).cast()) }
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, NOTHING_CHANGED) }
    }
}
