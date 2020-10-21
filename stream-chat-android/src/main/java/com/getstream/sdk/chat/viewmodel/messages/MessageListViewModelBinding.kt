@file:JvmName("MessageListViewModelBinding")

package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.DeleteMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.EndRegionReached
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.FlagMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.GiphyActionSelected
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.LastMessageRead
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.RetryMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.ThreadModeEntered

@JvmName("bind")
public fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    view.init(channel, currentUser)
    view.setEndRegionReachedHandler { onEvent(EndRegionReached) }
    view.setLastMessageReadHandler { onEvent(LastMessageRead) }
    view.setOnMessageDeleteHandler { onEvent(DeleteMessage(it)) }
    view.setOnStartThreadHandler { onEvent(ThreadModeEntered(it)) }
    view.setOnMessageFlagHandler { onEvent(FlagMessage(it)) }
    view.setOnSendGiphyHandler { message, giphyAction ->
        onEvent(GiphyActionSelected(message, giphyAction))
    }
    view.setOnMessageRetryHandler { onEvent(RetryMessage(it)) }

    state.observe(lifecycleOwner) { state ->
        if (state is MessageListViewModel.State.Result) {
            view.displayNewMessage(state.messageListItem)
        }
    }
}
