package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.channels.ChannelsView

fun ChannelsViewModel.bindView(view: ChannelsView, lifecycle: LifecycleOwner) {
    state.observe(lifecycle, Observer {
        when (it) {
            is ChannelsViewModel.State.Result -> {
                view.setChannels(it.channels)
                view.hideEmptyStateView()
                view.hideLoadingView()
            }
            is ChannelsViewModel.State.Loading -> {
                if (it.isLoading) {
                    view.hideEmptyStateView()
                    view.showLoadingView()
                } else {
                    view.hideLoadingView()
                }
            }
            ChannelsViewModel.State.NoChannelsAvailable -> {
                view.showEmptyStateView()
                view.hideLoadingView()
            }
        }
    })

    view.setOnEndReachedListener {
        onEvent(ChannelsViewModel.Event.ReachedEndOfList)
    }
}