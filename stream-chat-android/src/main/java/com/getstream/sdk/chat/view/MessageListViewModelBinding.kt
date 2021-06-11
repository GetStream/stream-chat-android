@file:JvmName("MessageListViewModelBinding")

package com.getstream.sdk.chat.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Transformations
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.DeleteMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.EndRegionReached
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.FlagMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.GiphyActionSelected
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.LastMessageRead
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.RetryMessage
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Event.ThreadModeEntered

/**
 * Binds [MessageListView] with [MessageListViewModel].
 * Sets the View's handlers and displays new messages based on the ViewModel's state.
 */
@JvmName("bind")
public fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    Transformations.switchMap(user) { user ->
        Transformations.map(channel) { channel ->
            channel to user
        }
    }.observe(lifecycleOwner) { (channel, user) ->
        if (user != null) {
            view.init(channel, user)
        } 
    }

    channel.observe(lifecycleOwner) {
        view.init(it, currentUser)
    }
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
        when (state) {
            is MessageListViewModel.State.Loading -> {
                view.hideEmptyStateView()
                view.showLoadingView()
            }
            is MessageListViewModel.State.Result -> {
                if (state.messageListItem.items.isEmpty()) {
                    view.showEmptyStateView()
                } else {
                    view.hideEmptyStateView()
                }
                view.displayNewMessage(state.messageListItem)
                view.hideLoadingView()
            }
        }
    }
    loadMoreLiveData.observe(lifecycleOwner, view::setLoadingMore)
    targetMessage.observe(lifecycleOwner) {
        view.scrollToMessage(it)
    }
}
