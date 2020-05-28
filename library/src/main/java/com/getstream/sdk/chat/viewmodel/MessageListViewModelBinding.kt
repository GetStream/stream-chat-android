package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageListView

fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    view.setEndRegionReachedListener { onEvent(Event.EndRegionReached) }
    view.setLastMessageReadListener { onEvent(Event.LastMessageRead) }
    view.setThreadModeSelectedListener { onEvent(Event.ThreadModeEntered(it)) }

    state.observe(lifecycleOwner, Observer {
        if (it is State.Result) {
            view.displayNewMessage(it.messageListItem)
        }
    })
}