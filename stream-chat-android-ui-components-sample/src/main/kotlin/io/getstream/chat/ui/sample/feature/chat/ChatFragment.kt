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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.common.model.DeleteMessage
import io.getstream.chat.android.common.model.EditMessage
import io.getstream.chat.android.common.model.SendAnyway
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel
import io.getstream.chat.android.ui.message.composer.viewmodel.bindView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatBinding
import io.getstream.chat.ui.sample.feature.chat.composer.CustomMessageComposerLeadingContent
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize
import java.util.Calendar

@OptIn(ExperimentalStreamChatApi::class)
class ChatFragment : Fragment() {

    private val args: ChatFragmentArgs by navArgs()

    private val factory: MessageListViewModelFactory by lazy {
        MessageListViewModelFactory(
            cid = args.cid,
            messageId = args.messageId,
            navigateToThreadViaNotification = true
        )
    }
    private val chatViewModelFactory: ChatViewModelFactory by lazy { ChatViewModelFactory(args.cid) }
    private val headerViewModel: MessageListHeaderViewModel by viewModels { factory }
    private val messageListViewModel: MessageListViewModel by viewModels { factory }
    private val messageInputViewModel: MessageInputViewModel by viewModels { factory }
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

    @OptIn(InternalStreamChatApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerViewModel.bindView(binding.messagesHeaderView, viewLifecycleOwner)
        initChatViewModel()
        initMessagesViewModel()
        configureBackButtonHandling()

        if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_MESSAGE_COMPOSER)) {
            initMessageComposerViewModel()
        } else {
            initMessageInputViewModel()
        }
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
        binding.messageInputView.isVisible = true
        messageInputViewModel.apply {
            bindView(binding.messageInputView, viewLifecycleOwner)
            messageListViewModel.mode.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageListViewModel.Mode.Thread -> {
                        headerViewModel.setActiveThread(it.parentMessage)
                        messageInputViewModel.setActiveThread(it.parentMessage)
                    }
                    is MessageListViewModel.Mode.Normal -> {
                        headerViewModel.resetThread()
                        messageInputViewModel.resetThread()
                    }
                }
            }
            binding.messageListView.setMessageEditHandler(::postMessageToEdit)
        }
    }

    private fun initMessageComposerViewModel() {
        binding.messageComposerView.isVisible = true
        messageComposerViewModel.apply {
            bindView(binding.messageComposerView, viewLifecycleOwner)
            messageListViewModel.mode.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageListViewModel.Mode.Thread -> {
                        headerViewModel.setActiveThread(it.parentMessage)
                        messageComposerViewModel.setMessageMode(MessageMode.MessageThread(it.parentMessage))
                    }
                    is MessageListViewModel.Mode.Normal -> {
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
            binding.messageListView.setModeratedMessageHandler { message, action ->
                when (action) {
                    DeleteMessage -> messageListViewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message))
                    EditMessage -> messageComposerViewModel.performMessageAction(Edit(message))
                    SendAnyway -> messageListViewModel.onEvent(MessageListViewModel.Event.RetryMessage(message))
                    else -> Unit
                }
            }
            binding.messageListView.setAttachmentReplyOptionClickHandler { result ->
                messageListViewModel.getMessageWithId(result.messageId)?.let { message ->
                    messageComposerViewModel.performMessageAction(Reply(message))
                }
            }
        }

        if (OVERRIDE_LEADING_CONTENT) {
            binding.messageComposerView.setLeadingContent(
                CustomMessageComposerLeadingContent(requireContext())
            )
        }
    }

    private fun initMessagesViewModel() {
        val calendar = Calendar.getInstance()
        messageListViewModel.apply {
            messageListViewModel.setDeletedMessageVisibility(
                deletedMessageVisibility = DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER
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
                if (result.isSuccess || result.isAlreadyExistsError()) {
                    ConfirmationDialogFragment.newMessageFlaggedInstance(requireContext())
                        .show(parentFragmentManager, null)
                }
            }

            setModeratedMessageHandler { message, action ->
                when (action) {
                    DeleteMessage -> messageListViewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message))
                    EditMessage -> messageInputViewModel.postMessageToEdit(message)
                    SendAnyway -> messageListViewModel.onEvent(MessageListViewModel.Event.RetryMessage(message))
                    else -> Unit
                }
            }
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

    private fun Result<Flag>.isAlreadyExistsError(): Boolean {
        if (!isError) {
            return false
        }
        val chatError = error() as ChatNetworkError
        return chatError.streamCode == 4
    }

    private companion object {
        private const val OVERRIDE_LEADING_CONTENT = false
    }
}
