package com.getstream.sdk.chat.binding

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.ChannelListView
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel

fun ChannelsViewModel.bindView(view: ChannelListView, lifecycle: LifecycleOwner) {

    state.observe(lifecycle, Observer {
        when (it) {
            is ChannelsViewModel.State.Result -> view.setChannels(it.channels)
            else -> {

            }
        }
    })

    view.setOnEndReachedListener { onAction(ChannelsViewModel.Action.LoadMore) }
}