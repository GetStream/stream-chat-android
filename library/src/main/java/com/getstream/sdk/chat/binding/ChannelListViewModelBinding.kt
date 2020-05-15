package com.getstream.sdk.chat.binding

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.ChannelListView
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel

fun ChannelsViewModel.bindView(view: ChannelListView, lifecycle: LifecycleOwner) {

    state.observe(lifecycle, Observer {
        Log.d("ChannelsViewModel", "ChannelsViewModel received state: ${it.javaClass}")
        when (it) {
            is ChannelsViewModel.State.Result -> view.setChannels(it.channels)
            else -> {

            }
        }
    })

    view.setOnEndReachedListener {
        Log.d("ChannelsViewModel", "ChannelsViewModel binding: end region reached")
        onAction(ChannelsViewModel.Action.ReachedEndOfList)
    }
}