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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

class MessagesActivity : BaseConnectedActivity() {

    private val factory by lazy {
        MessagesViewModelFactory(
            context = this,
            channelId = intent.getStringExtra(KEY_CHANNEL_ID) ?: "",
            deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
        )
    }

    private val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

    private val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
    private val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelId = intent.getStringExtra(KEY_CHANNEL_ID) ?: return

        setContent {
            ChatTheme(dateFormatter = ChatApp.dateFormatter) {
                MyCustomUi()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MyCustomUi() {
        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments
        val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState
        val user by listViewModel.user.collectAsState()
        val lazyListState = rememberLazyListState()

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    MyCustomComposer()
                }
            ) {
                MessageList(
                    modifier = Modifier
                        .padding(it)
                        .background(ChatTheme.colors.appBackground)
                        .fillMaxSize(),
                    viewModel = listViewModel,
                    lazyListState = if (listViewModel.currentMessagesState.parentMessageId != null) rememberLazyListState() else lazyListState,
                    onThreadClick = { message ->
                        composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                        listViewModel.openMessageThread(message)
                    },
                    onImagePreviewResult = { result ->
                        when (result?.resultType) {
                            ImagePreviewResultType.QUOTE -> {
                                val message = listViewModel.getMessageWithId(result.messageId)

                                if (message != null) {
                                    composerViewModel.performMessageAction(Reply(message))
                                }
                            }

                            ImagePreviewResultType.SHOW_IN_CHAT -> {
                            }
                            null -> Unit
                        }
                    }
                )
            }

            if (isShowingAttachments) {
                AttachmentsPicker(
                    attachmentsPickerViewModel = attachmentsPickerViewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(350.dp),
                    onAttachmentsSelected = { attachments ->
                        attachmentsPickerViewModel.changeAttachmentState(false)
                        composerViewModel.addSelectedAttachments(attachments)
                    },
                    onDismiss = {
                        attachmentsPickerViewModel.changeAttachmentState(false)
                        attachmentsPickerViewModel.dismissAttachments()
                    }
                )
            }

            if (selectedMessageState != null) {
                val selectedMessage = selectedMessageState.message
                when (selectedMessageState) {
                    is SelectedMessageOptionsState -> {
                        SelectedMessageMenu(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp)
                                .wrapContentSize(),
                            shape = ChatTheme.shapes.attachment,
                            messageOptions = defaultMessageOptionsState(
                                selectedMessage = selectedMessage,
                                currentUser = user,
                                isInThread = listViewModel.isInThread,
                                ownCapabilities = selectedMessageState.ownCapabilities
                            ),
                            message = selectedMessage,
                            ownCapabilities = selectedMessageState.ownCapabilities,
                            onMessageAction = { action ->
                                composerViewModel.performMessageAction(action)
                                listViewModel.performMessageAction(action)
                            },
                            onShowMoreReactionsSelected = {
                                listViewModel.selectExtendedReactions(selectedMessage)
                            },
                            onDismiss = { listViewModel.removeOverlay() }
                        )
                    }
                    is SelectedMessageReactionsState -> {
                        SelectedReactionsMenu(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp)
                                .wrapContentSize(),
                            shape = ChatTheme.shapes.attachment,
                            message = selectedMessage,
                            currentUser = user,
                            onMessageAction = { action ->
                                composerViewModel.performMessageAction(action)
                                listViewModel.performMessageAction(action)
                            },
                            onShowMoreReactionsSelected = {
                                listViewModel.selectExtendedReactions(selectedMessage)
                            },
                            onDismiss = { listViewModel.removeOverlay() },
                            ownCapabilities = selectedMessageState.ownCapabilities
                        )
                    }
                    is SelectedMessageReactionsPickerState -> {
                        ReactionsPicker(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp)
                                .wrapContentSize(),
                            shape = ChatTheme.shapes.attachment,
                            message = selectedMessage,
                            onMessageAction = { action ->
                                composerViewModel.performMessageAction(action)
                                listViewModel.performMessageAction(action)
                            },
                            onDismiss = { listViewModel.removeOverlay() }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MyCustomComposer() {
        val isOverlayShowing = remember {
            mutableStateOf(false)
        }

        MessageComposer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            viewModel = composerViewModel,
            integrations = {
                MyCustomIntegrations(
                    messageInputState = it,
                    onAttachmentsClick = { },
                    onCommandsClick = { },
                    onVoiceRecordingClicked = { isOverlayShowing.value = !isOverlayShowing.value },
                    ownCapabilities = composerViewModel.ownCapabilities.collectAsState().value
                )
            },
            input = { inputState ->
                if (isOverlayShowing.value) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 8.dp)
                            .background(Color.Blue, shape = ChatTheme.shapes.inputField)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .weight(1f)
                            .align(CenterVertically),
                        text = "overlay"
                    )
                } else {
                    MessageInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .weight(1f),
                        label = {
                            val text =
                                if (composerViewModel.ownCapabilities.collectAsState().value.contains(
                                        ChannelCapabilities.SEND_MESSAGE
                                    )
                                ) {
                                    stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_message_label)
                                } else {
                                    stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_cannot_send_messages_label)
                                }

                            Text(
                                text = text,
                                color = ChatTheme.colors.textLowEmphasis
                            )
                        },
                        messageComposerState = inputState,
                        onValueChange = { composerViewModel.setMessageInput(it) },
                        onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
                    )
                }
            },
        )
    }

    @Composable
    private fun MyCustomIntegrations(
        messageInputState: MessageComposerState,
        onAttachmentsClick: () -> Unit,
        onCommandsClick: () -> Unit,
        onVoiceRecordingClicked: () -> Unit,
        ownCapabilities: Set<String>,
    ) {
        val hasTextInput = messageInputState.inputValue.isNotEmpty()
        val hasAttachments = messageInputState.attachments.isNotEmpty()
        val hasCommandInput = messageInputState.inputValue.startsWith("/")
        val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
        val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

        val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
        val isCommandsButtonEnabled = !hasTextInput && !hasAttachments

        val canSendMessage = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
        val canSendAttachments = ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)

        if (canSendMessage) {
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canSendAttachments) {
                    IconButton(
                        enabled = isAttachmentsButtonEnabled,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp),
                        content = {
                            Icon(
                                painter = painterResource(id = io.getstream.chat.android.compose.R.drawable.stream_compose_ic_attachments),
                                contentDescription = stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_attachments),
                                tint = if (isAttachmentsButtonEnabled) {
                                    ChatTheme.colors.textLowEmphasis
                                } else {
                                    ChatTheme.colors.disabled
                                },
                            )
                        },
                        onClick = onAttachmentsClick
                    )
                }

                val commandsButtonTint = if (hasCommandSuggestions && isCommandsButtonEnabled) {
                    ChatTheme.colors.primaryAccent
                } else if (isCommandsButtonEnabled) {
                    ChatTheme.colors.textLowEmphasis
                } else {
                    ChatTheme.colors.disabled
                }

                IconButton(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    enabled = isCommandsButtonEnabled,
                    content = {
                        Icon(
                            painter = painterResource(id = io.getstream.chat.android.compose.R.drawable.stream_compose_ic_command),
                            contentDescription = null,
                            tint = commandsButtonTint,
                        )
                    },
                    onClick = onCommandsClick
                )

                IconButton(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_mic_24),
                            contentDescription = null,
                            tint = ChatTheme.colors.textLowEmphasis

                        )
                    },
                    onClick = onVoiceRecordingClicked
                )
            }
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        fun createIntent(context: Context, channelId: String): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}
