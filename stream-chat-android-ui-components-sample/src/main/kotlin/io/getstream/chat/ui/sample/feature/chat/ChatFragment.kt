/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.ui.sample.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.DefaultUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.DefaultUserQueryFilter
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.RemoteUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.list.DeleteMessage
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.EditMessage
import io.getstream.chat.android.ui.common.state.messages.list.SendAnyway
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatBinding
import io.getstream.chat.ui.sample.feature.chat.composer.CustomMessageComposerLeadingContent
import io.getstream.chat.ui.sample.feature.chat.messagelist.options.CustomMessageOptions
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import io.getstream.chat.ui.sample.util.extensions.notifyMessageChanged
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.launch
import java.util.Calendar

class ChatFragment : Fragment() {

    private val logger by taggedLogger("ChatFragment")

    private val args: ChatFragmentArgs by navArgs()

    private val defaultUserLookupHandler by lazy {
        DefaultUserLookupHandler(
            chatClient = ChatClient.instance(),
            channelCid = args.cid,
            localFilter = DefaultUserQueryFilter(
                transliterator = DefaultStreamTransliterator(transliterationId = "Cyrl-Latn"),
            ),
        )
    }

    private val remoteUserLookupHandler by lazy {
        RemoteUserLookupHandler(
            chatClient = ChatClient.instance(),
            channelCid = args.cid,
        )
    }

    private val factory: MessageListViewModelFactory by lazy {
        val chatClient = ChatClient.instance()
        MessageListViewModelFactory(
            context = requireContext().applicationContext,
            cid = args.cid,
            messageId = args.messageId,
            parentMessageId = args.parentMessageId,
            chatClient = chatClient,
            userLookupHandler = defaultUserLookupHandler,
        )
    }
    private val chatViewModelFactory: ChatViewModelFactory by lazy { ChatViewModelFactory(cid = args.cid) }
    private val headerViewModel: MessageListHeaderViewModel by viewModels { factory }
    private val messageListViewModel: MessageListViewModel by viewModels { factory }
    private val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }
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
        initMessageListViewModel()
        initMessageComposerViewModel()
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
                },
            )
        }
        binding.messagesHeaderView.setBackButtonClickListener {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
    }

    private fun initChatViewModel() {
        chatViewModel.members.observe(viewLifecycleOwner) { members ->
            binding.messagesHeaderView.apply {
                setAvatarClickListener {
                    chatViewModel.onAction(ChatViewModel.Action.HeaderClicked(members))
                }
                setTitleClickListener {
                    chatViewModel.onAction(ChatViewModel.Action.HeaderClicked(members))
                }
                setSubtitleClickListener {
                    chatViewModel.onAction(ChatViewModel.Action.HeaderClicked(members))
                }
            }
        }
        chatViewModel.navigationEvent.observe(
            viewLifecycleOwner,
            EventObserver { event ->
                when (event) {
                    is ChatViewModel.NavigationEvent.NavigateToChatInfo -> findNavController().navigateSafely(
                        ChatFragmentDirections.actionChatFragmentToChatInfoFragment(event.cid),
                    )
                    is ChatViewModel.NavigationEvent.NavigateToGroupChatInfo -> findNavController().navigateSafely(
                        ChatFragmentDirections.actionChatFragmentToGroupChatInfoFragment(event.cid),
                    )
                }
            },
        )

        messageListViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is MessageListViewModel.State.Result) {
                logger.v { "[onMessageListViewModelState] messageListItem: ${state.messageListItem.items.size}" }
                chatViewModel.onMessageListState(state.messageListItem)
            }
        }
        chatViewModel.translationEvent.observe(
            viewLifecycleOwner,
            EventObserver { message ->
                logger.v { "[onTranslationEvent] message: ${message.text}, i18n: ${message.i18n}" }
                binding.messageListView.notifyMessageChanged(message)
            },
        )
    }

    private fun initMessageComposerViewModel() {
        messageComposerViewModel.apply {
            bindView(binding.messageComposerView, viewLifecycleOwner)
            messageListViewModel.mode.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageMode.MessageThread -> {
                        headerViewModel.setActiveThread(it.parentMessage)
                        messageComposerViewModel.setMessageMode(MessageMode.MessageThread(it.parentMessage))
                    }
                    is MessageMode.Normal -> {
                        headerViewModel.resetThread()
                        messageComposerViewModel.leaveThread()
                    }
                }
            }

            binding.messageListView.setMessageReplyHandler { _, message ->
                messageComposerViewModel.performMessageAction(Reply(message))
            }
            binding.messageListView.setMessageEditHandler { message ->
                messageComposerViewModel.performMessageAction(Edit(message))
            }
            binding.messageListView.setAttachmentReplyOptionClickHandler { result ->
                messageListViewModel.getMessageById(result.messageId)?.let { message ->
                    messageComposerViewModel.performMessageAction(Reply(message))
                }
            }
        }

        if (OVERRIDE_LEADING_CONTENT) {
            binding.messageComposerView.setLeadingContent(
                CustomMessageComposerLeadingContent(requireContext()),
            )
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                messageComposerViewModel.messageMode.collect { messageMode ->
                    when (messageMode) {
                        is MessageMode.Normal -> { /* no-op */ }
                        is MessageMode.MessageThread -> { /* no-op */ }
                    }
                    val modeText = messageMode.javaClass.simpleName
                    logger.d { "[onMessageModeChange] messageMode: $modeText" }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                messageComposerViewModel.lastActiveAction.collect { messageAction ->
                    when (messageAction) {
                        is Edit -> { /* no-op */ }
                        is Reply -> { /* no-op */ }
                        else -> { /* no-op */ }
                    }
                    val actionText = messageAction?.javaClass?.simpleName
                    logger.d { "[onMessageActionChange] messageAction: $actionText" }
                }
            }
        }
    }

    private fun initMessageListViewModel() {
        val calendar = Calendar.getInstance()
        messageListViewModel.apply {
            messageListViewModel.setDeletedMessageVisibility(
                deletedMessageVisibility = DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER,
            )
            bindView(binding.messageListView, viewLifecycleOwner)
            setDateSeparatorHandler { previousMessage, message ->
                if (previousMessage == null) {
                    true
                } else {
                    shouldShowDateSeparator(calendar, previousMessage, message)
                }
            }
            setThreadDateSeparatorHandler { previousMessage, message ->
                if (previousMessage == null) {
                    false
                } else {
                    shouldShowDateSeparator(calendar, previousMessage, message)
                }
            }
            state.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageListViewModel.State.Loading -> Unit
                    is MessageListViewModel.State.Result -> Unit
                    is MessageListViewModel.State.NavigateUp -> {
                        val navigationHappened = findNavController().navigateUp()

                        if (!navigationHappened) {
                            findNavController()
                                .navigateSafely(ChatFragmentDirections.actionChatFragmentToHomeFragment())
                        }
                    }
                }
            }
        }
        binding.messageListView.apply {
            setConfirmDeleteMessageHandler { _, confirmCallback: () -> Unit ->
                ConfirmationDialogFragment.newDeleteMessageInstance(requireContext())
                    .apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener(confirmCallback::invoke)
                    }.show(parentFragmentManager, null)
            }

            setConfirmFlagMessageHandler { _, confirmCallback: () -> Unit ->
                ConfirmationDialogFragment.newFlagMessageInstance(requireContext())
                    .apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener(confirmCallback::invoke)
                    }.show(parentFragmentManager, null)
            }

            setFlagMessageResultHandler { result ->
                if (result is Result.Success || result.isAlreadyExistsError()) {
                    ConfirmationDialogFragment.newMessageFlaggedInstance(requireContext())
                        .show(parentFragmentManager, null)
                }
            }

            setModeratedMessageHandler { message, action ->
                when (action) {
                    DeleteMessage -> messageListViewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message))
                    EditMessage -> messageComposerViewModel.performMessageAction(Edit(message))
                    SendAnyway -> messageListViewModel.onEvent(MessageListViewModel.Event.RetryMessage(message))
                    else -> Unit
                }
            }

            setMessageOptionItemsFactory(CustomMessageOptions.optionFactory(context))
            setCustomActionHandler(
                CustomMessageOptions.actionHandler(
                    onTranslate = { chatViewModel.onAction(ChatViewModel.Action.Translate(it)) },
                    onClearTranslation = { chatViewModel.onAction(ChatViewModel.Action.ClearTranslation(it)) },
                    onDeleteForMe = {
                        ConfirmationDialogFragment.newDeleteMessageForMeInstance(requireContext())
                            .apply {
                                confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                                    chatViewModel.onAction(ChatViewModel.Action.DeleteMessageForMe(it))
                                }
                            }.show(parentFragmentManager, null)
                    },
                ),
            )
        }
    }

    private fun shouldShowDateSeparator(calendar: Calendar, previousMessage: Message, message: Message): Boolean {
        val (previousYear, previousDayOfYear) = calendar.run {
            time = previousMessage.getCreatedAtOrThrow()
            get(Calendar.YEAR) to get(Calendar.DAY_OF_YEAR)
        }
        val (year, dayOfYear) = calendar.run {
            time = message.getCreatedAtOrThrow()
            get(Calendar.YEAR) to get(Calendar.DAY_OF_YEAR)
        }
        return previousYear != year || previousDayOfYear != dayOfYear
    }

    private fun Result<Flag>.isAlreadyExistsError(): Boolean = when (this) {
        is Result.Success -> false
        is Result.Failure -> (value as Error.NetworkError).serverErrorCode == 4
    }

    private companion object {
        private const val OVERRIDE_LEADING_CONTENT = false
    }
}
