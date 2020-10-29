@file:JvmName("ChannelsViewModelBinding")

package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.view.channels.ChannelsView

/***
 * Binds [ChannelsView] with [ChannelsViewModel].
 * It starts updating view's state based on [ChannelsViewModel.state] and
 * sets view's [com.getstream.sdk.chat.view.channels.ChannelListView.EndReachedListener]
 * @see [ChannelsView.setOnEndReachedListener]
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
            ChannelsViewModel.State.NoChannelsAvailable -> {
                view.showEmptyStateView()
                view.hideLoadingView()
            }
        }
    }

    view.setOnEndReachedListener {
        onEvent(ChannelsViewModel.Event.ReachedEndOfList)
    }
}
