package io.getstream.chat.docs.cookbook.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.pinned.list.PinnedMessageListView
import io.getstream.chat.android.ui.pinned.list.viewmodel.PinnedMessageListViewModel
import io.getstream.chat.android.ui.pinned.list.viewmodel.PinnedMessageListViewModelFactory
import io.getstream.chat.android.ui.pinned.list.viewmodel.bindView

class PinnedMessageList : Fragment() {
    lateinit var pinnedMessageListView: PinnedMessageListView

    fun bindingPinnedMessageListViewWithViewModel() {
        // Create view model
        val viewModel: PinnedMessageListViewModel by viewModels {
            PinnedMessageListViewModelFactory(cid = "channelType:channelId")
        }
        // Bind with view
        viewModel.bindView(pinnedMessageListView, viewLifecycleOwner)
    }

    fun handlingPinnedMessageListViewActions() {
        pinnedMessageListView.setPinnedMessageSelectedListener { message ->
            // Handle pinned message click
        }
    }
}
