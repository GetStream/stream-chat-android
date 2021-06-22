package io.getstream.chat.docs.cookbook.ui.messagelist

import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView

class MessageListFragment : Fragment() {

    private lateinit var messageListView: MessageListView

    fun bindView() {
        val channelCID = "messaging:123"
        val viewModel = MessageListViewModel(cid = channelCID)
        viewModel.bindView(messageListView, this)
    }
}
