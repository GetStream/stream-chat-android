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
import androidx.compose.animation.Crossfade
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

@Composable
internal fun MessageComposerInputTrailingContent(
    state: MessageComposerState,
    recordingActions: AudioRecordingActions,
    onSendClick: (String, List<Attachment>) -> Unit,
) {
    val inputText = state.inputValue
    val coolDownTime = state.coolDownTime
    val validationErrors = state.validationErrors
    val attachments = state.attachments
    val isInEditMode = state.action is Edit

    val canSendMessage = state.canSendMessage()
    val isInputValid = (inputText.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty()
    val isRecording = state.recording !is RecordingState.Idle
    val hasValidContent = canSendMessage && isInputValid && !isRecording

    val isRecordAudioPermissionDeclared = LocalContext.current.isPermissionDeclared(Manifest.permission.RECORD_AUDIO)
    val isRecordingEnabled = isRecordAudioPermissionDeclared && ChatTheme.config.composer.audioRecordingEnabled

    val actionButton = when {
        coolDownTime > 0 -> ActionButton.CoolDown(coolDownTime)
        isInEditMode -> ActionButton.Save(enabled = hasValidContent)
        hasValidContent -> ActionButton.Send
        isRecordingEnabled -> ActionButton.Record
        else -> null
    }

    Crossfade(targetState = actionButton) { button ->
        when (button) {
            is ActionButton.CoolDown -> ChatTheme.componentFactory.MessageComposerCoolDownIndicator(
                modifier = Modifier.Companion,
                coolDownTime = button.coolDownTime,
            )

            is ActionButton.Save -> ChatTheme.componentFactory.MessageComposerSaveButton(
                enabled = button.enabled,
                onClick = { onSendClick(inputText, attachments) },
            )

            is ActionButton.Send -> ChatTheme.componentFactory.MessageComposerSendButton(
                onClick = { onSendClick(inputText, attachments) },
            )

            is ActionButton.Record -> ChatTheme.componentFactory.MessageComposerAudioRecordingButton(
                state = state.recording,
                recordingActions = recordingActions,
            )

            null -> Unit
        }
    }
}

private sealed interface ActionButton {
    data class CoolDown(val coolDownTime: Int) : ActionButton
    data class Save(val enabled: Boolean) : ActionButton
    data object Send : ActionButton
    data object Record : ActionButton
}

@Composable
internal fun MessageComposerSendButton(
    onClick: () -> Unit,
) {
    val sendButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.sendButton
    FilledIconButton(
        modifier = Modifier
            .size(sendButtonStyle.size)
            .padding(sendButtonStyle.padding)
            .testTag("Stream_ComposerSendButton"),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChatTheme.colors.accentPrimary,
            contentColor = ChatTheme.colors.textOnAccent,
        ),
        onClick = onClick,
    ) {
        Icon(
            painter = sendButtonStyle.icon.painter,
            contentDescription = stringResource(id = R.string.stream_compose_send_message),
        )
    }
}

@Composable
internal fun MessageComposerSaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val sendButtonStyle = ChatTheme.messageComposerTheme.actionsTheme.sendButton
    FilledIconButton(
        enabled = enabled,
        modifier = Modifier
            .size(sendButtonStyle.size)
            .padding(sendButtonStyle.padding)
            .testTag("Stream_ComposerSaveButton"),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChatTheme.colors.accentPrimary,
            contentColor = ChatTheme.colors.textOnAccent,
            disabledContainerColor = ChatTheme.colors.backgroundCoreDisabled,
            disabledContentColor = ChatTheme.colors.textDisabled,
        ),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
            contentDescription = stringResource(id = R.string.stream_compose_save_message),
        )
    }
}
