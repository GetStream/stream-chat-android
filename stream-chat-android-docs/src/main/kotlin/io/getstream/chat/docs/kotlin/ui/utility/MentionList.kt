package io.getstream.chat.docs.kotlin.ui.utility

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.feature.mentions.list.MentionListView
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModel
import io.getstream.chat.android.ui.viewmodel.mentions.MentionListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.mentions.bindView

/**
 * [Mention List View](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/)
 */
class MentionList : Fragment() {

    private lateinit var mentionListView: MentionListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/#usage)
     */
    fun usage() {
        val viewModel: MentionListViewModel by viewModels { MentionListViewModelFactory() }
        viewModel.bindView(mentionListView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/utility-components/mention-list-view/#handling-actions)
     */
    fun handlingActions() {
        mentionListView.setMentionSelectedListener { message ->
            // Handle a mention item being clicked
        }
    }
}
