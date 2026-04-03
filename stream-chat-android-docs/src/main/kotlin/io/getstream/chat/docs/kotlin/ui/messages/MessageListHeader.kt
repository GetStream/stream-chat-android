package io.getstream.chat.docs.kotlin.ui.messages

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.feature.messages.header.ChannelHeaderView
import io.getstream.chat.android.ui.viewmodel.messages.ChannelHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * [Message List Header](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/)
 */
private class MessageListHeader : Fragment() {

    private lateinit var channelHeaderView: ChannelHeaderView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#usage)
     */
    fun usage() {
        // Initialize ViewModel
        val viewModel: ChannelHeaderViewModel by viewModels {
            ChannelViewModelFactory(requireContext(), cid = "messaging:123")
        }

        // Bind the View and ViewModel
        viewModel.bindView(channelHeaderView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-header/#handling-actions)
     */
    fun handlingActions() {
        channelHeaderView.setBackButtonClickListener {
            // Handle back button click
        }
        channelHeaderView.setAvatarClickListener {
            // Handle avatar click
        }
        channelHeaderView.setTitleClickListener {
            // Handle title click
        }
        channelHeaderView.setSubtitleClickListener {
            // Handle subtitle click
        }
    }
}
