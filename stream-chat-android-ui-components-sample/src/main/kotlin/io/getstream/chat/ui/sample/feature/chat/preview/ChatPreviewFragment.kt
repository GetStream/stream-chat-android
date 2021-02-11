package io.getstream.chat.ui.sample.feature.chat.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.message.input.bindView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.android.ui.message.view.bindView
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatPreviewBinding
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize

class ChatPreviewFragment : Fragment() {

    private val args: ChatPreviewFragmentArgs by navArgs()
    private val viewModel: ChatPreviewViewModel by viewModels { ChatPreviewViewModelFactory(args.userData.id) }

    private var _binding: FragmentChatPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.messagesHeaderView.setBackButtonClickListener {
            findNavController().navigateUp()
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.cid != null) {
                binding.progressBar.isVisible = false
                initializeChatPreview(state.cid)
            }
        }
        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is ChatPreviewViewModel.UiEvent.NavigateToChat -> findNavController().navigateSafely(
                        ChatPreviewFragmentDirections.actionOpenChat(cid = it.cid),
                    )
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    private fun initializeChatPreview(cid: String) {
        val factory = MessageListViewModelFactory(cid)
        val messageListHeaderViewModel = factory.create(MessageListHeaderViewModel::class.java)
        val messageListViewModel = factory.create(MessageListViewModel::class.java)
        val messageInputViewModel = factory.create(MessageInputViewModel::class.java)

        binding.messagesHeaderView.apply {
            messageListHeaderViewModel.bindView(this, viewLifecycleOwner)

            setAvatarClickListener {
                navigateToChatInfo()
            }
            setTitleClickListener {
                navigateToChatInfo()
            }
            setSubtitleClickListener {
                navigateToChatInfo()
            }
        }

        binding.messageListView.apply {
            isVisible = true
            messageListViewModel.bindView(this, viewLifecycleOwner)
        }

        binding.messageInputView.apply {
            isVisible = true
            messageInputViewModel.bindView(this, viewLifecycleOwner)
            setOnSendButtonClickListener {
                viewModel.onAction(ChatPreviewViewModel.Action.MessageSent)
            }
        }
    }

    private fun navigateToChatInfo() {
        findNavController().navigateSafely(
            ChatPreviewFragmentDirections.actionOpenChatInfo(userData = args.userData, cid = null)
        )
    }
}
