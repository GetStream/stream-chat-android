package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.MessageListView

fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    view.init(channel, currentUser)
    view.setEndRegionReachedHandler { onEvent(MessageListViewModel.Event.EndRegionReached) }
    view.setLastMessageReadHandler { onEvent(MessageListViewModel.Event.LastMessageRead) }
    view.setOnMessageDeleteHandler { onEvent(MessageListViewModel.Event.DeleteMessage(it)) }
    view.setOnStartThreadHandler { onEvent(MessageListViewModel.Event.ThreadModeEntered(it)) }
    view.setOnMessageFlagHandler { onEvent(MessageListViewModel.Event.FlagMessage(it)) }
    view.setOnSendGiphyHandler { message, giphyAction ->
        onEvent(MessageListViewModel.Event.GiphyActionSelected(message, giphyAction))
    }

    state.observe(
        lifecycleOwner,
        Observer {
            if (it is MessageListViewModel.State.Result) { view.displayNewMessage(it.messageListItem) }
        }
    )
}
