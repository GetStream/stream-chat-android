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

package io.getstream.chat.android.compose.ui.messages.composer

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.AboveAnchorPopupPositionProvider
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

/**
 * Default MessageComposer component that relies on [MessageComposerViewModel] to handle data and
 * communicate various events.
 *
 * @param viewModel The ViewModel that provides pieces of data to show in the composer, like the
 * currently selected integration data or the user input. It also handles sending messages.
 * @param modifier Modifier for styling.
 * @param onSendMessage Handler when the user sends a message. By default it delegates this to the
 * ViewModel, but the user can override if they want more custom behavior.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onCommandsClick Handler for the default Commands integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onLinkPreviewClick Handler when the user taps on a link preview.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 * @param recordingActions The actions that can be performed on an audio recording.
 * @param headerContent The content shown at the top of the message composer.
 * @param footerContent The content shown at the bottom of the message composer.
 * @param mentionPopupContent Customizable composable that represents the mention suggestions popup.
 * @param commandPopupContent Customizable composable that represents the instant command suggestions popup.
 * @param integrations A view that represents custom integrations. By default, we provide
 * [DefaultComposerIntegrations], which show Attachments & Commands, but users can override this with
 * their own integrations, which they need to hook up to their own data providers and UI.
 * @param label Customizable composable that represents the input field label (hint).
 * @param input Customizable composable that represents the input field for the composer, [MessageInput] by default.
 * @param audioRecordingContent Customizable composable used for displaying audio recording information
 * while audio recording is in progress.
 * @param trailingContent Customizable composable that represents the trailing content of the composer, send button
 * by default.
 */
@Composable
public fun MessageComposer(
    viewModel: MessageComposerViewModel,
    modifier: Modifier = Modifier,
    onSendMessage: (Message) -> Unit = { viewModel.sendMessage(it) },
    onAttachmentsClick: () -> Unit = {},
    onCommandsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = { viewModel.setMessageInput(it) },
    onAttachmentRemoved: (Attachment) -> Unit = { viewModel.removeSelectedAttachment(it) },
    onCancelAction: () -> Unit = { viewModel.dismissMessageActions() },
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onMentionSelected: (User) -> Unit = { viewModel.selectMention(it) },
    onCommandSelected: (Command) -> Unit = { viewModel.selectCommand(it) },
    onAlsoSendToChannelSelected: (Boolean) -> Unit = { viewModel.setAlsoSendToChannel(it) },
    recordingActions: AudioRecordingActions = AudioRecordingActions.defaultActions(viewModel),
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerHeaderContent(
                state = it,
                onCancel = onCancelAction,
                onLinkPreviewClick = onLinkPreviewClick,
            )
        }
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerFooterContent(
                state = it,
                onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
            )
        }
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerMentionsPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerCommandsPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    integrations: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerIntegrations(
                state = it,
                onAttachmentsClick = onAttachmentsClick,
                onCommandsClick = onCommandsClick,
            )
        }
    },
    label: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerLabel(state = it)
    },
    input: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerInput(
                state = it,
                onInputChanged = onValueChange,
                onAttachmentRemoved = onAttachmentRemoved,
                label = label,
            )
        }
    },
    audioRecordingContent: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerAudioRecordingContent(
                state = it,
                recordingActions = recordingActions,
            )
        }
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerTrailingContent(
            state = it,
            onSendClick = { input, attachments ->
                val message = viewModel.buildNewMessage(input, attachments)
                onSendMessage(message)
            },
            recordingActions = recordingActions,
        )
    },
) {
    val messageComposerState by viewModel.messageComposerState.collectAsState()

    MessageComposer(
        modifier = modifier.safeDrawingPadding(),
        onSendMessage = { text, attachments ->
            val messageWithData = viewModel.buildNewMessage(text, attachments)

            onSendMessage(messageWithData)
        },
        onMentionSelected = onMentionSelected,
        onCommandSelected = onCommandSelected,
        onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        recordingActions = recordingActions,
        headerContent = headerContent,
        footerContent = footerContent,
        mentionPopupContent = mentionPopupContent,
        commandPopupContent = commandPopupContent,
        integrations = integrations,
        input = input,
        audioRecordingContent = audioRecordingContent,
        trailingContent = trailingContent,
        messageComposerState = messageComposerState,
        onCancelAction = onCancelAction,
    )
}

/**
 * Clean version of the [MessageComposer] that doesn't rely on ViewModels, so the user can provide a
 * manual way to handle and represent data and various operations.
 *
 * @param messageComposerState The state of the message input.
 * @param onSendMessage Handler when the user wants to send a message.
 * @param modifier Modifier for styling.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onCommandsClick Handler for the default Commands integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 * @param recordingActions The actions that can be performed on an audio recording.
 * @param headerContent The content shown at the top of the message composer.
 * @param footerContent The content shown at the bottom of the message composer.
 * @param mentionPopupContent Customizable composable that represents the mention suggestions popup.
 * @param commandPopupContent Customizable composable that represents the instant command suggestions popup.
 * @param integrations A view that represents custom integrations. By default, we provide
 * [DefaultComposerIntegrations], which show Attachments & Giphy, but users can override this with
 * their own integrations, which they need to hook up to their own data providers and UI.
 * @param label Customizable composable that represents the input field label (hint).
 * @param input Customizable composable that represents the input field for the composer, [MessageInput] by default.
 * @param audioRecordingContent Customizable composable used for displaying audio recording information
 * while audio recording is in progress.
 * @param trailingContent Customizable composable that represents the trailing content of the composer, send button
 * by default.
 */
@Composable
public fun MessageComposer(
    messageComposerState: MessageComposerState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    modifier: Modifier = Modifier,
    onAttachmentsClick: () -> Unit = {},
    onCommandsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = {},
    onAttachmentRemoved: (Attachment) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onMentionSelected: (User) -> Unit = {},
    onCommandSelected: (Command) -> Unit = {},
    onAlsoSendToChannelSelected: (Boolean) -> Unit = {},
    recordingActions: AudioRecordingActions = AudioRecordingActions.None,
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerHeaderContent(
                state = it,
                onCancel = onCancelAction,
                onLinkPreviewClick = onLinkPreviewClick,
            )
        }
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerFooterContent(
                state = it,
                onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
            )
        }
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerMentionsPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerCommandsPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    integrations: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerIntegrations(
                state = it,
                onAttachmentsClick = onAttachmentsClick,
                onCommandsClick = onCommandsClick,
            )
        }
    },
    label: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerLabel(state = it)
    },
    input: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerInput(
                state = it,
                onInputChanged = onValueChange,
                onAttachmentRemoved = onAttachmentRemoved,
                label = label,
            )
        }
    },
    audioRecordingContent: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerAudioRecordingContent(
                state = it,
                recordingActions = recordingActions,
            )
        }
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerTrailingContent(
            state = it,
            onSendClick = onSendMessage,
            recordingActions = recordingActions,
        )
    },
) {
    val (_, _, activeAction, validationErrors, mentionSuggestions, commandSuggestions) = messageComposerState
    val snackbarHostState = remember { SnackbarHostState() }

    val isRecording = messageComposerState.recording !is RecordingState.Idle

    MessageInputValidationError(
        validationErrors = validationErrors,
        snackbarHostState = snackbarHostState,
    )

    Surface(
        modifier = modifier,
        shadowElevation = 4.dp,
        color = ChatTheme.colors.barsBackground,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            headerContent(messageComposerState)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Bottom,
            ) {
                if (activeAction !is Edit) {
                    integrations(messageComposerState)
                } else {
                    Spacer(
                        modifier = Modifier.size(16.dp),
                    )
                }

                input(messageComposerState)

                if (isRecording) {
                    audioRecordingContent(messageComposerState)
                }

                trailingContent(messageComposerState)
            }

            footerContent(messageComposerState)
        }

        if (snackbarHostState.currentSnackbarData != null) {
            SnackbarPopup(snackbarHostState = snackbarHostState)
        }

        if (mentionSuggestions.isNotEmpty()) {
            mentionPopupContent(mentionSuggestions)
        }

        if (commandSuggestions.isNotEmpty()) {
            commandPopupContent(commandSuggestions)
        }
    }
}

/**
 * Represents the default content shown at the top of the message composer component.
 *
 * @param messageComposerState The state of the message composer.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onLinkPreviewClick Handler when the user taps on a link preview.
 */
@Composable
public fun DefaultMessageComposerHeaderContent(
    messageComposerState: MessageComposerState,
    onCancelAction: () -> Unit,
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
) {
    val activeAction = messageComposerState.action

    if (activeAction != null) {
        ChatTheme.componentFactory.MessageComposerMessageInputOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
            activeAction = activeAction,
            onCancel = onCancelAction,
        )
    }
    if (ChatTheme.isComposerLinkPreviewEnabled && messageComposerState.linkPreviews.isNotEmpty()) {
        ChatTheme.componentFactory.MessageComposerLinkPreview(
            modifier = Modifier,
            linkPreview = messageComposerState.linkPreviews.first(),
            onClick = onLinkPreviewClick,
        )
    }
}

/**
 * Represents the default content shown at the bottom of the message composer component.
 *
 * @param messageComposerState The state of the message composer.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 */
@Composable
public fun DefaultMessageComposerFooterContent(
    messageComposerState: MessageComposerState,
    onAlsoSendToChannelSelected: (Boolean) -> Unit,
) {
    if (messageComposerState.messageMode is MessageMode.MessageThread) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                modifier = Modifier.testTag("Stream_AlsoSendToChannel"),
                checked = messageComposerState.alsoSendToChannel,
                onCheckedChange = { onAlsoSendToChannelSelected(it) },
                colors = CheckboxDefaults.colors(
                    ChatTheme.colors.primaryAccent,
                    ChatTheme.colors.textLowEmphasis,
                ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.stream_compose_message_composer_show_in_channel),
                color = ChatTheme.colors.textLowEmphasis,
                textAlign = TextAlign.Center,
                style = ChatTheme.typography.body,
            )
        }
    }
}

/**
 * Composable that represents the message composer integrations (special actions).
 *
 * Currently just shows the Attachment picker action.
 *
 * @param messageInputState The state of the input.
 * @param onAttachmentsClick Handler when the user selects attachments.
 * @param onCommandsClick Handler when the user selects commands.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
@Composable
internal fun DefaultComposerIntegrations(
    messageInputState: MessageComposerState,
    onAttachmentsClick: () -> Unit,
    onCommandsClick: () -> Unit,
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

    val isRecording = messageInputState.recording !is RecordingState.Idle

    if (canSendMessage && !isRecording) {
        Row(
            modifier = Modifier
                .heightIn(min = ComposerActionContainerMinHeight)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (canSendAttachments) {
                with(ChatTheme.componentFactory) {
                    MessageComposerAttachmentsButton(
                        enabled = isAttachmentsButtonEnabled,
                        onClick = onAttachmentsClick,
                    )
                }
            }

            AnimatedVisibility(visible = messageInputState.hasCommands) {
                with(ChatTheme.componentFactory) {
                    MessageComposerCommandsButton(
                        hasCommandSuggestions = hasCommandSuggestions,
                        enabled = isCommandsButtonEnabled,
                        onClick = onCommandsClick,
                    )
                }
            }
        }
    } else {
        Spacer(modifier = Modifier.width(12.dp))
    }
}

/**
 * Default input field label that the user can override in [MessageComposer].
 *
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
@Composable
internal fun DefaultComposerLabel(ownCapabilities: Set<String>) {
    val text =
        if (ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)) {
            stringResource(id = R.string.stream_compose_message_label)
        } else {
            stringResource(id = R.string.stream_compose_cannot_send_messages_label)
        }

    Text(
        text = text,
        color = ChatTheme.colors.textLowEmphasis,
    )
}

/**
 * Represents the default input content of the Composer.
 *
 * @param label Customizable composable that represents the input field label (hint).
 * @param messageComposerState The state of the message input.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 */
@Composable
internal fun RowScope.DefaultComposerInputContent(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    label: @Composable (MessageComposerState) -> Unit,
) {
    val isRecording = messageComposerState.recording !is RecordingState.Idle
    MessageInput(
        modifier = if (isRecording) {
            Modifier.size(0.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .weight(1f)
        },
        label = label,
        messageComposerState = messageComposerState,
        onValueChange = onValueChange,
        onAttachmentRemoved = onAttachmentRemoved,
    )
}

/**
 * Represents the default trailing content for the Composer, which represent a send button or a cooldown timer.
 *
 * @param messageComposerState The state of the message input.
 * @param onSendMessage Handler when the user wants to send a message.
 * @param recordingActions The actions that can be performed on an audio recording.
 */
@Composable
internal fun DefaultMessageComposerTrailingContent(
    messageComposerState: MessageComposerState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    recordingActions: AudioRecordingActions,
) {
    val value = messageComposerState.inputValue
    val coolDownTime = messageComposerState.coolDownTime
    val validationErrors = messageComposerState.validationErrors
    val attachments = messageComposerState.attachments
    val ownCapabilities = messageComposerState.ownCapabilities
    val isInEditMode = messageComposerState.action is Edit

    val isRecordAudioPermissionDeclared = LocalContext.current.isPermissionDeclared(Manifest.permission.RECORD_AUDIO)
    val isRecordingEnabled = isRecordAudioPermissionDeclared && ChatTheme.messageComposerTheme.audioRecording.enabled
    val showRecordOverSend = ChatTheme.messageComposerTheme.audioRecording.showRecordButtonOverSend

    val isSendButtonEnabled = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
    val isInputValid by lazy { (value.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty() }

    if (coolDownTime > 0 && !isInEditMode) {
        ChatTheme.componentFactory.MessageComposerCoolDownIndicator(modifier = Modifier, coolDownTime = coolDownTime)
    } else {
        val isRecording = messageComposerState.recording !is RecordingState.Idle

        val sendEnabled = isSendButtonEnabled && isInputValid
        val sendVisible = when {
            !isRecordingEnabled -> true
            isRecording -> false
            showRecordOverSend -> sendEnabled
            else -> true
        }
        if (sendVisible) {
            Box(
                modifier = Modifier.heightIn(min = ComposerActionContainerMinHeight),
                contentAlignment = Center,
            ) {
                ChatTheme.componentFactory.MessageComposerSendButton(
                    enabled = sendEnabled,
                    isInputValid = isInputValid,
                    onClick = {
                        if (isInputValid) {
                            onSendMessage(value, attachments)
                        }
                    },
                )
            }
        }

        val recordVisible = when {
            !isRecordingEnabled -> false
            showRecordOverSend -> !sendEnabled
            else -> true
        }
        if (recordVisible) {
            ChatTheme.componentFactory.MessageComposerAudioRecordButton(
                state = messageComposerState.recording,
                recordingActions = recordingActions,
            )
        }
    }
}

/**
 * Default implementation of the "Attachments" button.
 */
@Composable
internal fun AttachmentsButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val attachmentsButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.attachmentsButton
    IconButton(
        enabled = enabled,
        modifier = Modifier
            .size(attachmentsButtonStyle.size)
            .padding(attachmentsButtonStyle.padding)
            .testTag("Stream_ComposerAttachmentsButton"),
        content = {
            Icon(
                modifier = Modifier.size(attachmentsButtonStyle.icon.size),
                painter = attachmentsButtonStyle.icon.painter,
                contentDescription = stringResource(id = R.string.stream_compose_attachments),
                tint = if (enabled) {
                    attachmentsButtonStyle.icon.tint
                } else {
                    ChatTheme.colors.disabled
                },
            )
        },
        onClick = onClick,
    )
}

/**
 * Default implementation of the "Commands" button.
 */
@Composable
internal fun CommandsButton(
    hasCommandSuggestions: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val commandsButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.commandsButton
    val tint = if (hasCommandSuggestions && enabled) {
        ChatTheme.colors.primaryAccent
    } else if (enabled) {
        commandsButtonStyle.icon.tint
    } else {
        ChatTheme.colors.disabled
    }
    IconButton(
        modifier = Modifier
            .size(commandsButtonStyle.size)
            .padding(commandsButtonStyle.padding)
            .testTag("Stream_ComposerCommandsButton"),
        enabled = enabled,
        content = {
            Icon(
                modifier = Modifier.size(commandsButtonStyle.icon.size),
                painter = commandsButtonStyle.icon.painter,
                contentDescription = null,
                tint = tint,
            )
        },
        onClick = onClick,
    )
}

/**
 * Default implementation of the "Send" button.
 */
@Composable
internal fun SendButton(
    enabled: Boolean,
    isInputValid: Boolean,
    onClick: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val sendButtonDescription = stringResource(id = R.string.stream_compose_cd_send_button)
    val sendButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.sendButton
    IconButton(
        modifier = Modifier
            .size(sendButtonStyle.size)
            .padding(sendButtonStyle.padding)
            .semantics { contentDescription = sendButtonDescription }
            .testTag("Stream_ComposerSendButton"),
        enabled = enabled,
        content = {
            Icon(
                modifier = Modifier
                    .size(sendButtonStyle.icon.size)
                    .mirrorRtl(layoutDirection = layoutDirection),
                painter = sendButtonStyle.icon.painter,
                contentDescription = stringResource(id = R.string.stream_compose_send_message),
                tint = if (isInputValid) ChatTheme.colors.primaryAccent else sendButtonStyle.icon.tint,
            )
        },
        onClick = onClick,
    )
}

/**
 * Shows a [Toast] with an error if one of the following constraints are violated:
 *
 * - The message length exceeds the maximum allowed message length.
 * - The number of selected attachments is too big.
 * - At least one of the attachments is too big.
 *
 * @param validationErrors The list of validation errors for the current user input.
 */
@Composable
private fun MessageInputValidationError(validationErrors: List<ValidationError>, snackbarHostState: SnackbarHostState) {
    if (validationErrors.isNotEmpty()) {
        val firstValidationError = validationErrors.first()

        val errorMessage = when (firstValidationError) {
            is ValidationError.MessageLengthExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_message_length,
                    firstValidationError.maxMessageLength,
                )
            }

            is ValidationError.AttachmentCountExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_attachment_count,
                    firstValidationError.maxAttachmentCount,
                )
            }

            is ValidationError.AttachmentSizeExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_file_size,
                    MediaStringUtil.convertFileSizeByteCount(firstValidationError.maxAttachmentSize),
                )
            }

            is ValidationError.ContainsLinksWhenNotAllowed -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_sending_links_not_allowed,
                )
            }
        }

        val context = LocalContext.current
        LaunchedEffect(validationErrors.size) {
            if (firstValidationError is ValidationError.ContainsLinksWhenNotAllowed ||
                firstValidationError is ValidationError.AttachmentSizeExceeded
            ) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = context.getString(R.string.stream_compose_ok),
                    duration = SnackbarDuration.Indefinite,
                )
            } else {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * A snackbar wrapped inside of a popup allowing it be
 * displayed above the Composable it's anchored to.
 *
 * @param snackbarHostState The state of the snackbar host. Contains
 * the snackbar data necessary to display the snackbar.
 */
@Composable
private fun SnackbarPopup(snackbarHostState: SnackbarHostState) {
    Popup(popupPositionProvider = AboveAnchorPopupPositionProvider()) {
        SnackbarHost(hostState = snackbarHostState)
    }
}

/**
 * Defines the minimum height for the MessageComposer action containers.
 * Used to ensure that container before the composer has the same min height as the container after the composer.
 */
private val ComposerActionContainerMinHeight = 44.dp

private fun Offset.toRestrictedCoordinates(): Pair<Float, Float> = x.coerceAtMost(0f) to y.coerceAtMost(0f)
