package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageListView

fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    view.setEndRegionReachedListener { onEvent(MessageListViewModel.Event.EndRegionReached) }
    view.setLastMessageReadListener { onEvent(MessageListViewModel.Event.LastMessageRead) }
    view.setThreadModeSelectedListener { onEvent(MessageListViewModel.Event.ThreadModeEntered(it)) }

    view.setChannel(channel)
    state.observe(lifecycleOwner, Observer {
        if (it is MessageListViewModel.State.Result) {
            view.displayNewMessage(it.messageListItem)
        }
    })
}