@file:JvmName("ChannelsViewModelBinding")

package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.channel.list.ChannelsView

@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelsView,
    lifecycle: LifecycleOwner
) {
    state.observe(lifecycle) { channelState ->
        when (channelState) {
            is ChannelsViewModel.State.Loading -> view.showLoadingView()

            is ChannelsViewModel.State.Result -> {
                view.setChannels(channelState.channels)
                view.hideLoadingView()
            }
        }
    }
}
