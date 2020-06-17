package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.ChannelListView
import io.getstream.chat.android.client.logger.ChatLogger

fun ChannelsViewModel.bindView(view: ChannelListView, lifecycle: LifecycleOwner) {
    state.observe(lifecycle, Observer {
        ChatLogger.instance.logD("ChannelsViewModel", "Received state: $it")
        when (it) {
            is ChannelsViewModel.State.Result -> view.setChannels(it.channels)
            else -> {

            }
        }
    })

    view.setOnEndReachedListener {
        onEvent(ChannelsViewModel.Event.ReachedEndOfList)
    }
}