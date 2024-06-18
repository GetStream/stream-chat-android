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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.state.messages.attachments.StatefulStreamMediaRecorder
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptionItemVisibility
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerTheme
import io.getstream.chat.android.compose.ui.theme.MessageOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ReactionOptionsTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.ReactionSortingByFirstReactionAt
import io.getstream.chat.android.models.ReactionSortingByLastReactionAt
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageOptionsState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsPickerState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsState
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import io.getstream.sdk.chat.audio.recording.MediaRecorderState
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder

class MessagesActivity : BaseConnectedActivity() {

    private val streamMediaRecorder: StreamMediaRecorder by lazy { DefaultStreamMediaRecorder(applicationContext) }
    private val statefulStreamMediaRecorder by lazy { StatefulStreamMediaRecorder(streamMediaRecorder) }

    private val factory by lazy {
        MessagesViewModelFactory(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
            isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
            deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
            messageId = intent.getStringExtra(KEY_MESSAGE_ID),
            parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID),
        )
    }

    private val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

    private val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
    private val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val colors = if (isSystemInDarkTheme()) StreamColors.defaultDarkColors() else StreamColors.defaultColors()
            val typography = StreamTypography.defaultTypography()
            ChatTheme(
                colors = colors,
                typography = typography,
                dateFormatter = ChatApp.dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
                allowUIAutomationTest = true,
                messageComposerTheme = MessageComposerTheme.defaultTheme(typography).let { messageComposerTheme ->
                    messageComposerTheme.copy(
                        attachmentCancelIcon = messageComposerTheme.attachmentCancelIcon.copy(
                            painter = painterResource(id = R.drawable.stream_compose_ic_clear),
                            tint = colors.overlayDark,
                            backgroundColor = colors.appBackground,
                        ),
                    )
                },
                reactionOptionsTheme = ReactionOptionsTheme.defaultTheme(),
                messageOptionsTheme = MessageOptionsTheme.defaultTheme(
                    optionVisibility = MessageOptionItemVisibility(),
                ),
            ) {
                MessagesScreen(
                    viewModelFactory = factory,
                    reactionSorting = ReactionSortingByLastReactionAt,
                    onBackPressed = { finish() },
                    onHeaderTitleClick = {},
                    // TODO
                    // statefulStreamMediaRecorder = statefulStreamMediaRecorder
                )

                // MyCustomUi()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (statefulStreamMediaRecorder.mediaRecorderState.value == MediaRecorderState.RECORDING) {
            streamMediaRecorder.stopRecording()
        } else {
            streamMediaRecorder.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamMediaRecorder.stopRecording()
    }

    @Composable
    fun MyCustomUi() {
        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments
        val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState
        val user by listViewModel.user.collectAsState()
        val lazyListState = rememberMessageListState()

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    MyCustomComposer()
                },
            ) {
                MessageList(
                    modifier = Modifier
                        .padding(it)
                        .background(ChatTheme.colors.appBackground)
                        .fillMaxSize(),
                    viewModel = listViewModel,
                    reactionSorting = ReactionSortingByFirstReactionAt,
                    messagesLazyListState = if (listViewModel.isInThread) rememberMessageListState() else lazyListState,
                    onThreadClick = { message ->
                        composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                        listViewModel.openMessageThread(message)
                    },
                    onMediaGalleryPreviewResult = { result ->
                        when (result?.resultType) {
                            MediaGalleryPreviewResultType.QUOTE -> {
                                val message = listViewModel.getMessageById(result.messageId)

                                if (message != null) {
                                    composerViewModel.performMessageAction(Reply(message))
                                }
                            }

                            MediaGalleryPreviewResultType.SHOW_IN_CHAT -> {
                            }

                            null -> Unit
                        }
                    },
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
                    },
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
                                ownCapabilities = selectedMessageState.ownCapabilities,
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
                            onDismiss = { listViewModel.removeOverlay() },
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
                            ownCapabilities = selectedMessageState.ownCapabilities,
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
                            onDismiss = { listViewModel.removeOverlay() },
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    @Composable
    fun MyCustomComposer() {
        MessageComposer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            viewModel = composerViewModel,
            statefulStreamMediaRecorder = statefulStreamMediaRecorder,
            integrations = {},
            input = { inputState ->
                MessageInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(7f)
                        .padding(start = 8.dp),
                    messageComposerState = inputState,
                    onValueChange = { composerViewModel.setMessageInput(it) },
                    onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
                    label = {
                        Row(
                            Modifier.wrapContentWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.stream_compose_ic_gallery),
                                contentDescription = null,
                            )

                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = "Type something",
                                color = ChatTheme.colors.textLowEmphasis,
                            )
                        }
                    },
                    innerTrailingContent = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(),
                                ) {
                                    val state = composerViewModel.messageComposerState.value

                                    composerViewModel.sendMessage(
                                        composerViewModel.buildNewMessage(
                                            state.inputValue,
                                            state.attachments,
                                        ),
                                    )
                                },
                            painter = painterResource(id = R.drawable.stream_compose_ic_send),
                            tint = ChatTheme.colors.primaryAccent,
                            contentDescription = null,
                        )
                    },
                )
            },
            trailingContent = { Spacer(modifier = Modifier.size(8.dp)) },
        )
    }

    companion object {
        private const val TAG = "MessagesActivity"
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String? = null,
            parentMessageId: String? = null,
        ): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }
}
