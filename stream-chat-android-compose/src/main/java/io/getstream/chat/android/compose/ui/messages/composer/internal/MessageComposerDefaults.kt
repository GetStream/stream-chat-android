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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import android.Manifest
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canUploadFile
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

/**
 * Defines the minimum height for the MessageComposer action containers.
 * Used to ensure that container before the composer has the same min height as the container after the composer.
 */
private val ComposerActionContainerMinHeight = 44.dp

@Composable
internal fun DefaultMessageComposerHeaderContent(
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
    val hasCommandInput = messageInputState.inputValue.startsWith("/")
    val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
    val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

    val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions

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
        ChatTheme.componentFactory.MessageComposerCoolDownIndicator(
            modifier = Modifier.Companion,
            coolDownTime = coolDownTime,
        )
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
                contentAlignment = Alignment.Center,
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
