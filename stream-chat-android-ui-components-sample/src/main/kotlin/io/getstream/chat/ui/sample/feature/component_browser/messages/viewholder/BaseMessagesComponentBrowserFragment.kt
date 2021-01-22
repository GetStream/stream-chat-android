package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListViewHolderBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

abstract class BaseMessagesComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageListViewHolderBinding? = null
    protected val binding get() = _binding!!

    protected val currentUser = randomUser()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComponentBrowserMessageListViewHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.messageListView.apply {
            setMessageClickListener {}
            setMessageLongClickListener {}
            setMessageRetryListener {}
            setThreadClickListener {}
            setAttachmentClickListener { _, _ -> }
            setAttachmentDownloadClickListener {}
            setReactionViewClickListener {}
            setUserClickListener {}

            init(Channel(), currentUser)
            displayNewMessages(MessageListItemWrapper(getItems()))
        }
    }

    protected abstract fun getItems(): List<MessageListItem>
}
