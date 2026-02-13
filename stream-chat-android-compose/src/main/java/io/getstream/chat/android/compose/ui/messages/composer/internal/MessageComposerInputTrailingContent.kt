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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
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

    // Show cooldown indicator if applicable
    if (coolDownTime > 0 && !isInEditMode) {
        ChatTheme.componentFactory.MessageComposerCoolDownIndicator(
            modifier = Modifier.Companion,
            coolDownTime = coolDownTime,
        )
        return
    }

    val isRecordAudioPermissionDeclared = LocalContext.current.isPermissionDeclared(Manifest.permission.RECORD_AUDIO)
    val isRecordingEnabled = isRecordAudioPermissionDeclared && ChatTheme.config.composer.audioRecordingEnabled
    val canSendMessage = state.canSendMessage()
    val isInputValid = (inputText.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty()
    val isRecording = state.recording !is RecordingState.Idle

    val shouldShowSendButton = canSendMessage && isInputValid && !isRecording
    val shouldShowRecordButton = isRecordingEnabled && !shouldShowSendButton

    val actionButton = when {
        shouldShowSendButton -> "send"
        shouldShowRecordButton -> "record"
        else -> null
    }

    Crossfade(targetState = actionButton) { button ->
        when (button) {
            "send" -> ChatTheme.componentFactory.MessageComposerSendButton(
                onClick = { onSendClick(inputText, attachments) },
            )

            "record" -> ChatTheme.componentFactory.MessageComposerAudioRecordButton(
                state = state.recording,
                recordingActions = recordingActions,
            )
        }
    }
}
