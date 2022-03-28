// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.channels

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView

/**
 * [Channel List Header](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-header/)
 */
private class ChannelListHeaderViewSnippets : Fragment() {

    private lateinit var channelListHeaderView: ChannelListHeaderView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-header/#usage)
     */
    fun usage() {
        // Get ViewModel
        val viewModel: ChannelListHeaderViewModel by viewModels()
        // Bind it with ChannelListHeaderView
        viewModel.bindView(channelListHeaderView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list-header/#handling-actions)
     */
    fun handlingActions() {
        channelListHeaderView.setOnActionButtonClickListener {
            // Handle action button click
        }
        channelListHeaderView.setOnUserAvatarClickListener {
            // Handle user avatar click
        }
    }
}
