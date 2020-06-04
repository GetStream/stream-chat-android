package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageListView

fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    view.init(channel, currentUser)
    view.setEndRegionReachedListener { onEvent(MessageListViewModel.Event.EndRegionReached) }
    view.setLastMessageReadListener { onEvent(MessageListViewModel.Event.LastMessageRead) }
    view.setOnMessageDeleteHandler { onEvent(MessageListViewModel.Event.DeleteMessage(it)) }
    view.setOnStartThreadHandler { onEvent(MessageListViewModel.Event.ThreadModeEntered(it)) }
    view.setOnMessageFlagHandler { onEvent(MessageListViewModel.Event.FlagMessage(it)) }

    state.observe(lifecycleOwner, Observer {
        if (it is MessageListViewModel.State.Result) { view.displayNewMessage(it.messageListItem) }
    })
}