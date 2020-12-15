package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemAdapter

public open class ChannelListItemViewHolderFactory {

    public fun createViewHolder(
        parentView: ViewGroup,
        channelItemType: ChannelListItemAdapter.ChannelItemType,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        deleteClickListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        style: ChannelListViewStyle?
    ): BaseChannelListItemViewHolder {
        return when (channelItemType) {
            ChannelListItemAdapter.ChannelItemType.DEFAULT -> createChannelViewHolder(
                parentView,
                channelClickListener,
                channelLongClickListener,
                deleteClickListener,
                userClickListener,
                style
            )

            ChannelListItemAdapter.ChannelItemType.LOADING_MORE -> createLoadingMoreViewHolder(parentView)
        }
    }

    public open fun createChannelViewHolder(
        parentView: ViewGroup,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        deleteClickListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        style: ChannelListViewStyle?
    ): BaseChannelListItemViewHolder {
        return ChannelViewHolder(
            parentView,
            channelClickListener,
            channelLongClickListener,
            deleteClickListener,
            userClickListener,
            style
        )
    }

    public open fun createLoadingMoreViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return LoadingMoreViewHolder(parentView)
    }
}
