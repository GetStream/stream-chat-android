package io.getstream.chat.ui.sample.feature.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelViewModelFactory
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.messages.header.bindView
import io.getstream.chat.android.ui.messages.view.bindView
import io.getstream.chat.android.ui.textinput.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatBinding
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

        messageListViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is MessageListViewModel.State.Result && state.messageListItem.isThread) {
                binding.messagesHeaderView.setThreadSubtitle(threadSubtitle(requireContext(), state.messageListItem))
            }
        }
    }

    private fun threadSubtitle(context: Context, messageWrapper: MessageListItemWrapper): String {
        val users = threadUsers(messageWrapper)

        val subtitleMessage = if (users.size == 1) {
            users.first().name
        } else {
            chatViewModel.getChannelName()
        }

        return String.format(context.getString(R.string.stream_ui_subtitle_thread_reply_single_user), subtitleMessage)
    }

    private fun threadUsers(messageWrapper: MessageListItemWrapper): Set<User> {
        return messageWrapper.items
            .filterIsInstance<MessageListItem.MessageItem>()
            .filter { message -> message.isTheirs }
            .map { messageItem -> messageItem.message.user }
            .toSet()
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
            binding.messageListView.setOnMessageEditHandler {
                editMessage.postValue(it)
            }
        }

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

        // set external suggestion view which is displayed over message list
        binding.messageInputView.setSuggestionListView(binding.suggestionListView)
    }

    private fun initMessagesViewModel() {
        messageListViewModel
            .apply {
                bindView(binding.messageListView, viewLifecycleOwner)
            }
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
