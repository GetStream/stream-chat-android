package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelItemType

public open class ChannelListItemViewHolderFactory {

    public fun createViewHolder(
        parentView: ViewGroup,
        channelItemType: ChannelItemType,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        deleteClickListener: ChannelListView.ChannelClickListener,
        moreOptionsClickListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        swipeEventListener: ChannelListView.ViewHolderSwipeDelegate,
        style: ChannelListViewStyle?
    ): BaseChannelListItemViewHolder {
        return when (channelItemType) {
            ChannelItemType.DEFAULT -> createChannelViewHolder(
                parentView,
                channelClickListener,
                channelLongClickListener,
                deleteClickListener,
                moreOptionsClickListener,
                userClickListener,
                swipeEventListener,
                style
            )

            ChannelItemType.LOADING_MORE -> createLoadingMoreViewHolder(parentView)
        }
    }

    public open fun createChannelViewHolder(
        parentView: ViewGroup,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        deleteClickListener: ChannelListView.ChannelClickListener,
        moreOptionsClickListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        swipeEventListener: ChannelListView.ViewHolderSwipeDelegate,
        style: ChannelListViewStyle?
    ): BaseChannelListItemViewHolder {
        return ChannelItemViewHolder(
            parentView,
            channelClickListener,
            channelLongClickListener,
            deleteClickListener,
            moreOptionsClickListener,
            userClickListener,
            swipeEventListener,
            style
        )
    }

    public open fun createLoadingMoreViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return LoadingMoreViewHolder(parentView)
    }
}
