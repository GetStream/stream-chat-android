package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelItemType

public open class ChannelListItemViewHolderFactory {

    public lateinit var listenerContainer: ChannelListListenerContainer
        internal set

    public lateinit var style: ChannelListViewStyle
        internal set

    internal fun createViewHolder(
        parentView: ViewGroup,
        channelItemType: ChannelItemType,
    ): BaseChannelListItemViewHolder {
        return when (channelItemType) {
            ChannelItemType.DEFAULT -> createChannelViewHolder(parentView)
            ChannelItemType.LOADING_MORE -> createLoadingMoreViewHolder(parentView)
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
