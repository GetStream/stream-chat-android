package io.getstream.chat.docs.kotlin

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView

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
}
