package io.getstream.chat.docs.kotlin

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.textinput.MessageInputView
import io.getstream.chat.android.ui.textinput.bindView

class Android {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    inner class ChannelListHeader(private val channelListHeaderView: ChannelListHeaderView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: ChannelListHeaderViewModel by viewModels()
            // Bind it with ChannelListHeaderView
            viewModel.bindView(channelListHeaderView, viewLifecycleOwner)
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/message_input_view_neo">Message Input View</a>
     */
    inner class MessageInput(private val messageInputView: MessageInputView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: MessageInputViewModel by viewModels()
            // Bind it with ChannelListHeaderView
            viewModel.bindView(messageInputView, viewLifecycleOwner)
        }
    }
}
