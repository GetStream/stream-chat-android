/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.LocalMessageComposerFloatingStyleEnabled
import io.getstream.chat.android.compose.ui.util.AboveAnchorPopupPositionProvider
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canUploadFile
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
 * @param leadingContent The content shown at the start of the message composer.
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
    leadingContent: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerLeadingContent(
                state = it,
                onAttachmentsClick = onAttachmentsClick,
            )
        }
    },
    label: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerLabel(state = it)
    },
    input: @Composable RowScope.(MessageComposerState) -> Unit = { state ->
        with(ChatTheme.componentFactory) {
            MessageComposerInput(
                state = state,
                onInputChanged = onValueChange,
                onAttachmentRemoved = onAttachmentRemoved,
                onLinkPreviewClick = onLinkPreviewClick,
                label = label,
                onCancel = onCancelAction,
                onSendClick = { input, attachments ->
                    val message = viewModel.buildNewMessage(input, attachments)
                    onSendMessage(message)
                },
                recordingActions = recordingActions,
                leadingContent = {
                    ChatTheme.componentFactory.MessageComposerInputLeadingContent(
                        state = state,
                    )
                },
                trailingContent = {
                    ChatTheme.componentFactory.MessageComposerInputTrailingContent(
                        state = state,
                        onSendClick = { input, attachments ->
                            val message = viewModel.buildNewMessage(input, attachments)
                            onSendMessage(message)
                        },
                        recordingActions = recordingActions,
                    )
                },
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
        )
    },
) {
    val messageComposerState by viewModel.messageComposerState.collectAsState()

    ChatTheme.componentFactory.MessageComposer(
        modifier = modifier,
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
        leadingContent = leadingContent,
        input = input,
        audioRecordingContent = audioRecordingContent,
        trailingContent = trailingContent,
        messageComposerState = messageComposerState,
        onCancelAction = onCancelAction,
        onAttachmentsClick = onAttachmentsClick,
        onCommandsClick = onCommandsClick,
        onValueChange = onValueChange,
        onAttachmentRemoved = onAttachmentRemoved,
        onLinkPreviewClick = onLinkPreviewClick,
        label = label,
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
 * @param leadingContent The content shown at the start of the message composer.
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
    leadingContent: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerLeadingContent(
                state = it,
                onAttachmentsClick = onAttachmentsClick,
            )
        }
    },
    label: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerLabel(state = it)
    },
    input: @Composable RowScope.(MessageComposerState) -> Unit = { state ->
        with(ChatTheme.componentFactory) {
            MessageComposerInput(
                state = state,
                onInputChanged = onValueChange,
                onAttachmentRemoved = onAttachmentRemoved,
                onCancel = onCancelAction,
                onLinkPreviewClick = onLinkPreviewClick,
                label = label,
                onSendClick = onSendMessage,
                recordingActions = recordingActions,
                leadingContent = {
                    ChatTheme.componentFactory.MessageComposerInputLeadingContent(
                        state = state,
                    )
                },
                trailingContent = {
                    ChatTheme.componentFactory.MessageComposerInputTrailingContent(
                        state = state,
                        onSendClick = onSendMessage,
                        recordingActions = recordingActions,
                    )
                },
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
        )
    },
) {
    val activeAction = messageComposerState.action
    val validationErrors = messageComposerState.validationErrors
    val mentionSuggestions = messageComposerState.mentionSuggestions
    val commandSuggestions = messageComposerState.commandSuggestions
    val snackbarHostState = remember { SnackbarHostState() }

    val isRecording = messageComposerState.recording !is RecordingState.Idle

    MessageInputValidationError(
        validationErrors = validationErrors,
        snackbarHostState = snackbarHostState,
    )

    MessageComposerSurface(
        modifier = modifier,
        floatingStyleEnabled = ChatTheme.messageComposerFloatingStyleEnabled,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            headerContent(messageComposerState)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Bottom,
            ) {
                if (activeAction !is Edit) {
                    leadingContent(messageComposerState)
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

@Composable
private fun MessageComposerSurface(
    modifier: Modifier,
    floatingStyleEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (floatingStyleEnabled) {
        Box(
            modifier = modifier,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier,
            shadowElevation = ChatTheme.dimens.messageComposerShadowElevation,
            color = ChatTheme.colors.barsBackground,
            content = content,
        )
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
                .padding(start = 12.dp),
            activeAction = activeAction,
            onCancel = onCancelAction,
        )
    }
}

/**
 * Represents the default content shown at the bottom of the message composer component.
 *
 * @param messageComposerState The state of the message composer.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 */
@Deprecated(
    message = "Use ChatComponentFactory.MessageComposerFooterContent to customize the footer content",
    level = DeprecationLevel.WARNING,
)
@Composable
public fun DefaultMessageComposerFooterContent(
    messageComposerState: MessageComposerState,
    onAlsoSendToChannelSelected: (Boolean) -> Unit,
) {
    if (messageComposerState.messageMode is MessageMode.MessageThread) {
        DefaultMessageComposerFooterInThreadMode(
            alsoSendToChannel = messageComposerState.alsoSendToChannel,
            onAlsoSendToChannelChanged = onAlsoSendToChannelSelected,
        )
    }
}

@Composable
internal fun DefaultMessageComposerFooterInThreadMode(
    alsoSendToChannel: Boolean,
    onAlsoSendToChannelChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            modifier = Modifier.testTag("Stream_AlsoSendToChannel"),
            checked = alsoSendToChannel,
            onCheckedChange = onAlsoSendToChannelChanged,
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

@Composable
internal fun DefaultMessageComposerLeadingContent(
    messageInputState: MessageComposerState,
    onAttachmentsClick: () -> Unit,
) {
    val hasTextInput = messageInputState.inputValue.isNotEmpty()
    val hasAttachments = messageInputState.attachments.isNotEmpty()
    val hasCommandInput = messageInputState.inputValue.startsWith("/")
    val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
    val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

    val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
    val isCommandsButtonEnabled = !hasTextInput && !hasAttachments

    val canSendMessage = messageInputState.canSendMessage()

    val isRecording = messageInputState.recording !is RecordingState.Idle

    if (canSendMessage && !isRecording) {
        Row(
            modifier = Modifier
                .heightIn(min = ComposerActionContainerMinHeight)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val canUploadFile = messageInputState.canUploadFile()
            if (canUploadFile) {
                with(ChatTheme.componentFactory) {
                    MessageComposerAttachmentsButton(
                        enabled = isAttachmentsButtonEnabled,
                        onClick = onAttachmentsClick,
                    )
                }
            }
        }
    }
}

/**
 * Default input field label that the user can override in [MessageComposer].
 *
 * @param state The state of the message input.
 */
@Composable
internal fun DefaultComposerLabel(state: MessageComposerState) {
    val text = if (state.canSendMessage()) {
        stringResource(id = R.string.stream_compose_message_label)
    } else {
        stringResource(id = R.string.stream_compose_cannot_send_messages_label)
    }

    Text(
        text = text,
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.messageComposerTheme.inputField.textStyle,
    )
}

@Composable
internal fun RowScope.DefaultMessageComposerInput(
    modifier: Modifier,
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    onCancelAction: () -> Unit,
    onLinkPreviewClick: ((LinkPreview) -> Unit)?,
    label: @Composable (MessageComposerState) -> Unit,
    onSendClick: (String, List<Attachment>) -> Unit,
    recordingActions: AudioRecordingActions,
    leadingContent: @Composable RowScope.() -> Unit = {
        ChatTheme.componentFactory.MessageComposerInputLeadingContent(
            state = messageComposerState,
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        ChatTheme.componentFactory.MessageComposerInputTrailingContent(
            state = messageComposerState,
            onSendClick = onSendClick,
            recordingActions = recordingActions,
        )
    },
) {
    val isRecording = messageComposerState.recording !is RecordingState.Idle
    MessageInput(
        modifier = if (isRecording) {
            modifier.size(0.dp)
        } else {
            modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        },
        label = label,
        messageComposerState = messageComposerState,
        onValueChange = onValueChange,
        onAttachmentRemoved = onAttachmentRemoved,
        onCancelAction = onCancelAction,
        onLinkPreviewClick = onLinkPreviewClick,
        onSendClick = onSendClick,
        recordingActions = recordingActions,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
    )
}

@Composable
internal fun DefaultMessageComposerInputTrailingContent(
    messageComposerState: MessageComposerState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    recordingActions: AudioRecordingActions,
) {
    val value = messageComposerState.inputValue
    val coolDownTime = messageComposerState.coolDownTime
    val validationErrors = messageComposerState.validationErrors
    val attachments = messageComposerState.attachments
    val isInEditMode = messageComposerState.action is Edit

    val isRecordAudioPermissionDeclared = LocalContext.current.isPermissionDeclared(Manifest.permission.RECORD_AUDIO)
    val isRecordingEnabled = isRecordAudioPermissionDeclared && ChatTheme.messageComposerTheme.audioRecording.enabled
    val showRecordOverSend = ChatTheme.messageComposerTheme.audioRecording.showRecordButtonOverSend

    val canSendMessage = messageComposerState.canSendMessage()
    val isInputValid by lazy { (value.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty() }

    if (coolDownTime > 0 && !isInEditMode) {
        ChatTheme.componentFactory.MessageComposerCoolDownIndicator(modifier = Modifier, coolDownTime = coolDownTime)
    } else {
        val isRecording = messageComposerState.recording !is RecordingState.Idle

        val sendButtonEnabled = canSendMessage && isInputValid
        val sendButtonVisible = when {
            !isRecordingEnabled -> true
            isRecording -> false
            showRecordOverSend -> sendButtonEnabled
            else -> true
        }
        if (sendButtonVisible) {
            Box(
                modifier = Modifier.heightIn(min = ComposerActionContainerMinHeight),
                contentAlignment = Center,
            ) {
                ChatTheme.componentFactory.MessageComposerSendButton(
                    enabled = sendButtonEnabled,
                    isInputValid = isInputValid,
                    onClick = {
                        if (isInputValid) {
                            onSendMessage(value, attachments)
                        }
                    },
                )
            }
        }

        val recordButtonVisible = when {
            !canSendMessage || !isRecordingEnabled -> false
            showRecordOverSend -> !sendButtonEnabled
            else -> true
        }
        if (recordButtonVisible) {
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
                contentDescription = stringResource(R.string.stream_compose_message_composer_instant_commands),
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
    val sendButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.sendButton
    IconButton(
        modifier = Modifier
            .size(sendButtonStyle.size)
            .padding(sendButtonStyle.padding)
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

@Preview
@Composable
private fun MessageComposerPlaceholderPreview() {
    ChatTheme {
        MessageComposerPlaceholder()
    }
}

@Composable
internal fun MessageComposerPlaceholder() {
    MessageComposer(
        messageComposerState = PreviewMessageComposerState,
        onSendMessage = { _, _ -> },
    )
}

@Preview
@Composable
private fun MessageComposerFilledPreview() {
    ChatTheme {
        MessageComposerFilled()
    }
}

@Composable
internal fun MessageComposerFilled() {
    MessageComposer(
        messageComposerState = PreviewMessageComposerState.copy(
            inputValue = "Hello word",
        ),
        onSendMessage = { _, _ -> },
    )
}

@Preview
@Composable
private fun MessageComposerOverflowPreview() {
    ChatTheme {
        MessageComposerOverflow()
    }
}

@Composable
internal fun MessageComposerOverflow() {
    MessageComposer(
        messageComposerState = PreviewMessageComposerState.copy(
            inputValue = "I’ve been thinking about our plan for the next few weeks, " +
                "and I wanted to check in with you about it. There are a few things I’d like to get aligned on, " +
                "especially how we want to divide the work and what the priorities should be. " +
                "I feel like we’ve made good progress so far, " +
                "but there are still a couple of details that keep popping up, " +
                "and it would be nice to sort them out before they turn into bigger issues. " +
                "Let me know when you have a moment so we can go through everything together",
        ),
        onSendMessage = { _, _ -> },
    )
}

@Preview
@Composable
private fun MessageComposerSlowModePreview() {
    ChatTheme {
        MessageComposerSlowMode()
    }
}

@Composable
internal fun MessageComposerSlowMode() {
    MessageComposer(
        messageComposerState = PreviewMessageComposerState.copy(
            inputValue = "Slow mode, wait 9s",
            coolDownTime = 9,
        ),
        onSendMessage = { _, _ -> },
    )
}

@Preview
@Composable
private fun MessageComposerFloatingPreview() {
    ChatTheme {
        MessageComposerFloating()
    }
}

@Composable
internal fun MessageComposerFloating() {
    CompositionLocalProvider(LocalMessageComposerFloatingStyleEnabled provides true) {
        MessageComposer(
            messageComposerState = PreviewMessageComposerState,
            onSendMessage = { _, _ -> },
        )
    }
}

private val PreviewMessageComposerState = MessageComposerState(
    ownCapabilities = ChannelCapabilities.toSet(),
    hasCommands = true,
)
