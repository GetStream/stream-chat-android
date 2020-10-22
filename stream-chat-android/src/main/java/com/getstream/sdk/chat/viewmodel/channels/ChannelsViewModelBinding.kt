@file:JvmName("ChannelsViewModelBinding")

package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.view.channels.ChannelsView
import io.getstream.chat.android.client.logger.ChatLogger

@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelsView,
    lifecycle: LifecycleOwner
): ChannelsViewModel = apply {
    state.observe(lifecycle) {
        ChatLogger.Companion.get("${this::class.java.simpleName}@{${System.identityHashCode(this)}}")
            .logW("-> View@{${System.identityHashCode(view)}}")

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
