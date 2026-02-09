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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState

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
    isAttachmentPickerVisible: Boolean,
    onAttachmentsClick: () -> Unit,
) {
    val hasCommandInput = messageInputState.inputValue.startsWith("/")
    val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
    val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

    val isAddButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions

    val canSendMessage = messageInputState.canSendMessage()

    val isRecording = messageInputState.recording !is RecordingState.Idle

    val canUploadFile = messageInputState.canUploadFile()

    if (canSendMessage && !isRecording && canUploadFile) {
        val attachmentsButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.attachmentsButton
        val iconRotation by animateFloatAsState(
            targetValue = if (isAttachmentPickerVisible) OpenAttachmentPickerButtonRotation else 0f,
        )
        FilledIconButton(
            enabled = isAddButtonEnabled,
            modifier = Modifier
                .padding(end = 8.dp)
                .border(
                    width = 1.dp,
                    color = ChatTheme.colors.borders,
                    shape = CircleShape,
                )
                .then(
                    if (ChatTheme.messageComposerFloatingStyleEnabled) {
                        Modifier.shadow(6.dp, shape = CircleShape)
                    } else {
                        Modifier
                    },
                )
                .size(attachmentsButtonStyle.size)
                .padding(attachmentsButtonStyle.padding)
                .testTag("Stream_ComposerAttachmentsButton"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = ChatTheme.colors.barsBackground,
                disabledContainerColor = ChatTheme.colors.barsBackground,
                contentColor = attachmentsButtonStyle.icon.tint,
                disabledContentColor = ChatTheme.colors.disabled,
            ),
            onClick = onAttachmentsClick,
        ) {
            Icon(
                modifier = Modifier
                    .size(attachmentsButtonStyle.icon.size)
                    .graphicsLayer { rotationZ = iconRotation },
                painter = attachmentsButtonStyle.icon.painter,
                contentDescription = stringResource(id = R.string.stream_compose_attachments),
            )
        }
    }
}

private const val OpenAttachmentPickerButtonRotation = 225f

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

@Suppress("LongParameterList")
@Composable
internal fun RowScope.DefaultMessageComposerInput(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    onLinkPreviewClick: ((LinkPreview) -> Unit)?,
    onCancelAction: () -> Unit,
    onCancelLinkPreviewClick: (() -> Unit)? = null,
    label: @Composable (MessageComposerState) -> Unit,
    onSendClick: (String, List<Attachment>) -> Unit,
    recordingActions: AudioRecordingActions,
    leadingContent: @Composable RowScope.() -> Unit = {
        ChatTheme.componentFactory.MessageComposerInputLeadingContent(
            state = messageComposerState,
        )
    },
    centerContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        ChatTheme.componentFactory.MessageComposerInputCenterContent(
            state = messageComposerState,
            onValueChange = onValueChange,
            modifier = modifier,
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        ChatTheme.componentFactory.MessageComposerInputTrailingContent(
            state = messageComposerState,
            recordingActions = recordingActions,
            onSendClick = onSendClick,
        )
    },
) {
    val isRecording = messageComposerState.recording !is RecordingState.Idle
    if (!isRecording) {
        MessageInput(
            modifier = Modifier.weight(1f),
            label = label,
            messageComposerState = messageComposerState,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved,
            onCancelAction = onCancelAction,
            onLinkPreviewClick = onLinkPreviewClick,
            onCancelLinkPreviewClick = onCancelLinkPreviewClick,
            onSendClick = onSendClick,
            recordingActions = recordingActions,
            leadingContent = leadingContent,
            centerContent = centerContent,
            trailingContent = trailingContent,
        )
    }
}

/**
 * Default implementation of the "Send" button.
 */
@Composable
internal fun SendButton(
    onClick: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val sendButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.sendButton
    FilledIconButton(
        modifier = Modifier
            .size(sendButtonStyle.size)
            .padding(sendButtonStyle.padding)
            .testTag("Stream_ComposerSendButton"),
        content = {
            Icon(
                modifier = Modifier
                    .size(sendButtonStyle.icon.size)
                    .mirrorRtl(layoutDirection = layoutDirection),
                painter = sendButtonStyle.icon.painter,
                contentDescription = stringResource(id = R.string.stream_compose_send_message),
            )
        },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChatTheme.colors.primaryAccent,
            contentColor = Color.White,
        ),
        onClick = onClick,
    )
}
