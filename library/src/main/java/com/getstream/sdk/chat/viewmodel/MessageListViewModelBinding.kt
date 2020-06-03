package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.view.Dialog.MessageMoreActionDialog
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.Message

fun MessageListViewModel.bindView(view: MessageListView, lifecycleOwner: LifecycleOwner) {
    view.setEndRegionReachedListener { onEvent(MessageListViewModel.Event.EndRegionReached) }
    view.setLastMessageReadListener { onEvent(MessageListViewModel.Event.LastMessageRead) }
    view.setThreadModeSelectedListener { onEvent(MessageListViewModel.Event.ThreadModeEntered(it)) }

    view.setChannel(channel)
    view.setMessageActionDelegate(object : MessageMoreActionDialog.MessageActionDelegate {
        override fun onMessageDelete(message: Message?) {
            message?.let {
                onEvent(MessageListViewModel.Event.DeleteMessage(it))
            }
        }

        override fun onMessageEdit(message: Message) {
            TODO("Not implemented")
        }

        override fun onStartThread(parent: Message?) {
            TODO("Not implemented")
        }

    })

    state.observe(lifecycleOwner, Observer {
        if (it is MessageListViewModel.State.Result) {
            view.displayNewMessage(it.messageListItem)
        }
    })
}