package io.getstream.chat.docs.kotlin.ui

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * [Overview](https://getstream.io/chat/docs/sdk/android/ui/overview/)
 */
class Overview : AppCompatActivity() {
    val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId") // 1
    val messageListViewModel: MessageListViewModel by viewModels { factory } // 2

    fun viewModelsInitialization(messageListView: MessageListView, viewLifecycleOwner: LifecycleOwner) {
        messageListViewModel.bindView(messageListView, viewLifecycleOwner) // 3
    }
}
