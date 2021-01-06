@file:JvmName("ChannelsViewModelBinding")

package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.ui.channel.list.ChannelsView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem

@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelsView,
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

        val logger = ChatLogger.get("LOADINGMORE")
        if (it.loadingMore) {
            logger.logD("Show Loading More")
            view.showLoadingMore()
        } else {
            logger.logD("Hide Loading More")
            view.hideLoadingMore()
        }
    }

    view.setOnEndReachedListener {
        onEvent(ChannelsViewModel.Event.ReachedEndOfList)
    }
}
