package io.getstream.chat.docs.kotlin.ui.guides

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

/**
 * [Building A Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-message-list-screen/)
 */
class BuildingAMessageListScreen: Fragment() {

    private lateinit var messageListView: MessageListView
    private lateinit var messageListHeaderView: MessageListHeaderView
    private lateinit var messageInputView: MessageInputView

    fun usage() {
        // Create view models
        val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageInputViewModel: MessageInputViewModel by viewModels { factory }

        // Bind view models
        messageListHeaderViewModel.bindView(messageListHeaderView, viewLifecycleOwner)
        messageListViewModel.bindView(messageListView, viewLifecycleOwner)
        messageInputViewModel.bindView(messageInputView, viewLifecycleOwner)

        // Let both message list header and message input know when we open a thread
        messageListViewModel.mode.observe(this) { mode ->
            when (mode) {
                is MessageListViewModel.Mode.Thread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageInputViewModel.setActiveThread(mode.parentMessage)
                }
                MessageListViewModel.Mode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageInputViewModel.resetThread()
                }
            }
        }

        // Let the message input know when we are editing a message
        messageListView.setMessageEditHandler(messageInputViewModel::postMessageToEdit)

        // Handle navigate up state
        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                // Handle navigate up
            }
        }

        // Handle back button behaviour correctly when you're in a thread
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        messageListHeaderView.setBackButtonClickListener(backHandler)

        // You should also consider overriding default Activity's back button behaviour
    }
}
