package io.getstream.chat.docs.kotlin.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * [Overview](https://getstream.io/chat/docs/sdk/android/ui/overview/)
 */
class Overview : Fragment() {

    private lateinit var messageListView: MessageListView

    /**
     * [ViewModels](https://getstream.io/chat/docs/sdk/android/ui/overview/#viewmodels)
     */
    fun viewModels() {
        // 1
        val factory = MessageListViewModelFactory(requireContext(), cid = "messaging:123")
        // 2
        val viewModel: MessageListViewModel by viewModels { factory }
        // 3
        viewModel.bindView(messageListView, viewLifecycleOwner)
    }
}
