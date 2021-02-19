package io.getstream.chat.docs.cookbook.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.mention.list.MentionListView
import io.getstream.chat.android.ui.mention.list.viewmodel.MentionListViewModel
import io.getstream.chat.android.ui.mention.list.viewmodel.bindView

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#mention-list-view">Mention List View</a>
 */
class MentionList : Fragment() {
    lateinit var mentionListView: MentionListView

    fun bindingMentionListViewWithViewModel() {
        // Create view model
        val viewModel: MentionListViewModel by viewModels()
        // Bind with view
        viewModel.bindView(mentionListView, viewLifecycleOwner)
    }

    fun handlingMentionListViewActions() {
        mentionListView.setMentionSelectedListener { message ->
            // Handle mention click
        }
    }
}
