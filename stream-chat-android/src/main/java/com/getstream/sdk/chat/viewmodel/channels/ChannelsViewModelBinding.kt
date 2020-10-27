@file:JvmName("ChannelsViewModelBinding")

package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.view.channels.ChannelsView
import io.getstream.chat.android.client.utils.PerformanceUtils

@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelsView,
    lifecycle: LifecycleOwner
): ChannelsViewModel = apply {
    var shown = false
    PerformanceUtils.startTask("Load channels")
    state.observe(lifecycle) {
        when (it) {
            is ChannelsViewModel.State.Result -> {
                if (!shown) {
                    shown = true
                    PerformanceUtils.stopTask("Load channels")
                }
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
