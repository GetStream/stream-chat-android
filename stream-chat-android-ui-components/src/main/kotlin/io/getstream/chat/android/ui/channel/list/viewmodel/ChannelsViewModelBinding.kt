@file:JvmName("ChannelsViewModelBinding")

package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem

@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelListView,
    lifecycle: LifecycleOwner
) {
    state.observe(lifecycle) { channelState ->
        when (channelState) {
            is ChannelsViewModel.State.NoChannelsAvailable -> {
                view.showEmptyStateView()
                view.hideLoadingView()
            }
            is ChannelsViewModel.State.Loading -> {
                view.hideEmptyStateView()
                if (!view.hasChannels()) {
                    view.showLoadingView()
                }
            }
            is ChannelsViewModel.State.Result -> {
                channelState
                    .channels
                    .map(ChannelListItem::ChannelItem)
                    .let(view::setChannels)

                view.hideLoadingView()
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
        onEvent(ChannelsViewModel.Event.ReachedEndOfList)
    }
}
