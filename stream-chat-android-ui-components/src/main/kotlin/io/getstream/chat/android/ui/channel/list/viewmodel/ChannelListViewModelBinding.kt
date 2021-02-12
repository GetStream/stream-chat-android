@file:JvmName("ChannelListViewModelBinding")

package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem

@JvmName("bind")
public fun ChannelListViewModel.bindView(
    view: ChannelListView,
    lifecycle: LifecycleOwner
) {
    state.observe(lifecycle) { channelState ->
        if (channelState.isLoading) {
            view.hideEmptyStateView()
            view.showLoadingView()
        } else {
            view.hideLoadingView()
            if (channelState.channels.isEmpty()) {
                view.showEmptyStateView()
            } else {
                channelState
                    .channels
                    .map(ChannelListItem::ChannelItem)
                    .let(view::setChannels)
                view.hideEmptyStateView()
            }
        }
    }

    paginationState.observe(lifecycle) {
        view.setPaginationEnabled(!it.endOfChannels && !it.loadingMore)

        if (it.loadingMore) {
            view.showLoadingMore()
        } else {
            view.hideLoadingMore()
        }
    }

    view.setOnEndReachedListener {
        onAction(ChannelListViewModel.Action.ReachedEndOfList)
    }
}
