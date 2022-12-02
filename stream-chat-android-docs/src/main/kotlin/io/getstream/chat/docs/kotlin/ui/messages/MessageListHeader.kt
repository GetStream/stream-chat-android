package io.getstream.chat.docs.kotlin.ui.messages

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * [Message List Header](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/)
 */
private class MessageListHeader : Fragment() {

    private lateinit var messageListHeaderView: MessageListHeaderView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#usage)
     */
    fun usage() {
        // Initialize ViewModel
        val viewModel: MessageListHeaderViewModel by viewModels {
            MessageListViewModelFactory(cid = "messaging:123")
        }

        // Bind the View and ViewModel
        viewModel.bindView(messageListHeaderView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#handling-actions)
     */
    fun handlingActions() {
        messageListHeaderView.setBackButtonClickListener {
            // Handle back button click
        }
        messageListHeaderView.setAvatarClickListener {
            // Handle avatar click
        }
        messageListHeaderView.setTitleClickListener {
            // Handle title click
        }
        messageListHeaderView.setSubtitleClickListener {
            // Handle subtitle click
        }
    }
}
