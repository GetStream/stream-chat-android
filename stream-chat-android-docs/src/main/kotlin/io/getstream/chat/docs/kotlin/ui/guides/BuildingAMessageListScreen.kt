package io.getstream.chat.docs.kotlin.ui.guides

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.header.ChannelHeaderView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.viewmodel.messages.ChannelHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * [Building A Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/guides/building-message-list-screen/)
 */
class BuildingAMessageListScreen : Fragment() {

    private lateinit var messageListView: MessageListView
    private lateinit var channelHeaderView: ChannelHeaderView
    private lateinit var messageComposerView: MessageComposerView

    fun usage() {
        // Create ViewModels for the Views
        val factory = ChannelViewModelFactory(requireContext(), cid = "messaging:123")
        val channelHeaderViewModel: ChannelHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

        // Bind the ViewModels with the Views
        channelHeaderViewModel.bindView(channelHeaderView, viewLifecycleOwner)
        messageListViewModel.bindView(messageListView, viewLifecycleOwner)
        messageComposerViewModel.bindView(messageComposerView, viewLifecycleOwner)

        // Let both message list header and message input know when we open a thread
        messageListViewModel.mode.observe(viewLifecycleOwner) { mode ->
            when (mode) {
                is MessageMode.MessageThread -> {
                    channelHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                }
                is MessageMode.Normal -> {
                    channelHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                }
            }
        }

        // Let the message composer know when we are replying to a message
        messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }

        // Let the message composer know when we are editing a message
        messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }

        // Handle navigate up state
        messageListViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                requireActivity().finish()
            }
        }

        // Handle back button behaviour correctly when you're in a thread
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        channelHeaderView.setBackButtonClickListener(backHandler)

        // Override the default Activity's back button behaviour
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backHandler()
                }
            }
        )
    }
}
