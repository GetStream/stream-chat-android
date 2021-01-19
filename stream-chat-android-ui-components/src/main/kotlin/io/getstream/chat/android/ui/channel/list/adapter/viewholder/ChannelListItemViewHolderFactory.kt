package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemType

public open class ChannelListItemViewHolderFactory {

    public lateinit var listenerContainer: ChannelListListenerContainer
        internal set

    public lateinit var style: ChannelListViewStyle
        internal set

    /**
     * Returns a view type value based on the type and contents of the given [item].
     * The view type returned here will be used as a parameter in [createViewHolder].
     *
     * For built-in view types, see [ChannelListItemType] and its constants.
     */
    public open fun getItemViewType(item: ChannelListItem): Int {
        return when (item) {
            is ChannelListItem.LoadingMoreItem -> ChannelListItemType.LOADING_MORE.ordinal
            is ChannelListItem.ChannelItem -> ChannelListItemType.DEFAULT.ordinal
        }
    }

    /**
     * Creates a new ViewHolder to be used in the Message List.
     * The [viewType] parameter is determined by [getItemViewType].
     */
    public open fun createViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseChannelListItemViewHolder {
        return when (ChannelListItemType.values()[viewType]) {
            ChannelListItemType.DEFAULT -> createChannelViewHolder(parentView)
            ChannelListItemType.LOADING_MORE -> createLoadingMoreViewHolder(parentView)
        }
    }

    public open fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return ChannelViewHolder(
            parentView,
            listenerContainer.channelClickListener,
            listenerContainer.channelLongClickListener,
            listenerContainer.deleteClickListener,
            listenerContainer.moreOptionsClickListener,
            listenerContainer.userClickListener,
            listenerContainer.swipeListener,
            style,
        )
    }

    public open fun createLoadingMoreViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return ChannelListLoadingMoreViewHolder(parentView)
    }
}
