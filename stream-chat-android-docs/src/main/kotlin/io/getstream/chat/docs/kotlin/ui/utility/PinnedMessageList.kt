package io.getstream.chat.docs.kotlin.ui.utility

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.feature.pinned.list.PinnedMessageListView
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.ui.viewmodel.pinned.PinnedMessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.pinned.bindView

/**
 * [Pinned Message List View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/pinned-message-list-view/)
 */
class PinnedMessageList : Fragment() {

    private lateinit var pinnedMessageListView: PinnedMessageListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/utility-components/pinned-message-list-view/#usage)
     */
    fun usage() {
        val viewModel: PinnedMessageListViewModel by viewModels {
            PinnedMessageListViewModelFactory(cid = "messaging:123")
        }
        viewModel.bindView(pinnedMessageListView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/pinned-message-list-view/#handling-actions)
     */
    fun handlingActions() {
        pinnedMessageListView.setPinnedMessageSelectedListener { message ->
            // Handle a pinned message item being clicked
        }
    }
}
