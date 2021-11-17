package io.getstream.videosample.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.videosample.R
import io.getstream.videosample.recycler.ItemsAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [MessageListViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessageListViewFragment : Fragment() {

    private val factory: MessageListViewModelFactory =
        MessageListViewModelFactory(cid = "messaging:sample-app-channel-109")

    private val messageListViewModel: MessageListViewModel by viewModels { factory }
    private val messageInputViewModel: MessageInputViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_message_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageListView = view.findViewById<MessageListView>(R.id.messageListView)
        val messageInputView = view.findViewById<MessageInputView>(R.id.messageInputView)

        messageListViewModel.bindView(messageListView, this)
        messageInputViewModel.bindView(messageInputView, this)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MessageListViewFragment()
    }
}
