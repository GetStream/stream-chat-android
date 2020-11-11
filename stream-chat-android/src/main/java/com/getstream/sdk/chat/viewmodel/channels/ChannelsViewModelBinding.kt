@file:JvmName("ChannelsViewModelBinding")

package com.getstream.sdk.chat.viewmodel.channels

import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.view.channels.ChannelsView

/***
 * Binds [ChannelsView] with [ChannelsViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to
 * the ViewModel, to load more channels as the View is scrolled.
 */
@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelsView,
    lifecycle: LifecycleOwner
): ChannelsViewModel = apply {
    state.observe(lifecycle) {
        when (it) {
            is ChannelsViewModel.State.Result -> {
                view.setChannels(it.channels)
                view.hideEmptyStateView()
                view.hideLoadingView()
            }
            is ChannelsViewModel.State.Loading -> {
                view.hideEmptyStateView()
                view.showLoadingView()
            }
            is ChannelsViewModel.State.NoChannelsAvailable -> {
                view.showEmptyStateView()
                view.hideLoadingView()
            }
        }
    }
    paginationState.observe(lifecycle) {
        view.setPaginationEnabled(!it.endOfChannels && !it.loadingMore)
    }
    view.setOnEndReachedListener {
        onEvent(ChannelsViewModel.Event.ReachedEndOfList)
    }
    typingEvents.observe(lifecycle) { (channelId, users) -> view.showTypingInChannel(channelId, users) }
}
