package io.getstream.chat.docs.cookbook.ui.messagelist

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

class MessageListFragment : Fragment() {

    private lateinit var messageListView: MessageListView

    val lifecycleOwner: LifecycleOwner = this

    // 1. Init view model
    val viewModel: MessageListViewModel by viewModels {
        MessageListViewModelFactory(cid = "messaging:123")
    }

    fun bindView() {
        // 2. Bind view and viewModel
        viewModel.bindView(messageListView, lifecycleOwner)
    }
}
