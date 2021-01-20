package io.getstream.chat.ui.sample.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelViewModelFactory
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.messages.header.bindView
import io.getstream.chat.android.ui.messages.view.bindView
import io.getstream.chat.android.ui.textinput.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatBinding
import io.getstream.chat.ui.sample.feature.home.HomeFragmentDirections
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize

class ChatFragment : Fragment() {

    private val args: ChatFragmentArgs by navArgs()

    private val factory: ChannelViewModelFactory by lazy { ChannelViewModelFactory(args.cid, args.messageId) }
    private val chatViewModelFactory: ChatViewModelFactory by lazy { ChatViewModelFactory(args.cid) }
    private val headerViewModel: ChannelHeaderViewModel by viewModels { factory }
    private val messageListViewModel: MessageListViewModel by viewModels { factory }
    private val messageInputViewModel: MessageInputViewModel by viewModels { factory }
    private val chatViewModel: ChatViewModel by viewModels { chatViewModelFactory }

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerViewModel.bindView(binding.messagesHeaderView, viewLifecycleOwner)
        initChatViewModel()
        initMessagesViewModel()
        initMessageInputViewModel()
        configureBackButtonHandling()
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    private fun configureBackButtonHandling() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
                    }
                }
            )
        }
        binding.messagesHeaderView.setBackButtonClickListener {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
    }

    private fun initChatViewModel() {
        binding.messagesHeaderView.apply {
            setAvatarClickListener {
                chatViewModel.onAction(ChatViewModel.Action.HeaderClicked)
            }
            setTitleClickListener {
                chatViewModel.onAction(ChatViewModel.Action.HeaderClicked)
            }
            setSubtitleClickListener {
                chatViewModel.onAction(ChatViewModel.Action.HeaderClicked)
            }
        }
        chatViewModel.navigationEvent.observe(
            viewLifecycleOwner,
            EventObserver { event ->
                when (event) {
                    is ChatViewModel.NavigationEvent.NavigateToChatInfo -> findNavController().navigateSafely(
                        ChatFragmentDirections.actionChatFragmentToChatInfoFragment(event.cid)
                    )
                    is ChatViewModel.NavigationEvent.NavigateToGroupChatInfo -> findNavController().navigateSafely(
                        ChatFragmentDirections.actionChatFragmentToGroupChatInfoFragment(event.cid)
                    )
                }
            }
        )
    }

    private fun initMessageInputViewModel() {
        messageInputViewModel.apply {
            bindView(binding.messageInputView, viewLifecycleOwner)
            messageListViewModel.mode.observe(
                viewLifecycleOwner,
                {
                    when (it) {
                        is MessageListViewModel.Mode.Thread -> {
                            headerViewModel.setActiveThread(it.parentMessage)
                            messageInputViewModel.setActiveThread(it.parentMessage)
                        }
                        is MessageListViewModel.Mode.Normal -> {
                            headerViewModel.setActiveThread(null)
                            messageInputViewModel.resetThread()
                        }
                    }
                }
            )
            binding.messageListView.apply {
                setOnMessageEditHandler {
                    editMessage.postValue(it)
                }
                setOnAttachmentReplyOptionClickHandler {
                    TODO("Not yet implemented")
                }
                setOnAttachmentShowInChatOptionClickHandler {
                    requireActivity().findNavController(R.id.hostFragmentContainer)
                        .navigateSafely(HomeFragmentDirections.actionOpenChat(it.cid, it.messageId))
                }
            }
        }

        // set external suggestion view which is displayed over message list
        binding.messageInputView.setSuggestionListView(binding.suggestionListView)
    }

    private fun initMessagesViewModel() {
        messageListViewModel
            .apply { bindView(binding.messageListView, viewLifecycleOwner) } // TODO replace with new design message list
            .apply {
                state.observe(
                    viewLifecycleOwner,
                    {
                        when (it) {
                            is MessageListViewModel.State.NavigateUp -> findNavController().navigateUp()
                        }
                    }
                )
            }
    }
}
