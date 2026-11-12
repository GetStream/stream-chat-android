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
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.composer.LocalMessageComposerScheduleAction
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerAudioRecordingButtonParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerCoolDownIndicatorParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerSaveButtonParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerSendButtonParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
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
    sendActionLabel: String? = null,
    saveActionLabel: String? = null,
) {
    val inputText = state.inputValue
    val coolDownTime = state.coolDownTime
    val validationErrors = state.validationErrors
    val attachments = state.attachments
    val isInEditMode = state.action is Edit
    val hasActiveCommand = state.activeCommand != null

    val canSendMessage = state.canSendMessage()
    val isInputValid = (inputText.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty()
    val isRecording = state.recording !is RecordingState.Idle
    val hasValidContent = canSendMessage && isInputValid && !isRecording

    val isRecordAudioPermissionDeclared = LocalContext.current.isPermissionDeclared(Manifest.permission.RECORD_AUDIO)
    val isRecordingEnabled = isRecordAudioPermissionDeclared && ChatTheme.config.composer.audioRecordingEnabled

    val actionButton = when {
        coolDownTime > 0 -> ActionButton.CoolDown(coolDownTime)
        isInEditMode || hasActiveCommand -> ActionButton.Save(enabled = hasValidContent)
        hasValidContent -> ActionButton.Send
        isRecordingEnabled -> ActionButton.Record
        else -> null
    }

    val scheduleAction = LocalMessageComposerScheduleAction.current

    Crossfade(targetState = actionButton) { button ->
        when (button) {
            is ActionButton.CoolDown -> ChatTheme.componentFactory.MessageComposerCoolDownIndicator(
                params = MessageComposerCoolDownIndicatorParams(
                    coolDownTime = button.coolDownTime,
                ),
            )

            is ActionButton.Save -> ChatTheme.componentFactory.MessageComposerSaveButton(
                params = MessageComposerSaveButtonParams(
                    enabled = button.enabled,
                    onClick = { onSendClick(inputText, attachments) },
                    onClickLabel = saveActionLabel,
                ),
            )

            is ActionButton.Send -> ChatTheme.componentFactory.MessageComposerSendButton(
                params = MessageComposerSendButtonParams(
                    onClick = { onSendClick(inputText, attachments) },
                    onClickLabel = sendActionLabel,
                    onLongClick = scheduleAction,
                    onLongClickLabel = scheduleAction?.let {
                        stringResource(R.string.stream_compose_schedule_message_title)
                    },
                ),
            )

            is ActionButton.Record -> ChatTheme.componentFactory.MessageComposerAudioRecordingButton(
                params = MessageComposerAudioRecordingButtonParams(
                    state = state.recording,
                    recordingActions = recordingActions,
                ),
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
    onClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val resolvedLabel = onClickLabel ?: stringResource(R.string.stream_compose_message_composer_send_action)
    if (onLongClick != null) {
        MessageComposerSendButtonWithLongClick(
            onClick = onClick,
            onClickLabel = resolvedLabel,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel,
        )
        return
    }
    FilledIconButton(
        modifier = Modifier
            .size(48.dp)
            .padding(StreamTokens.spacingXs)
            .testTag("Stream_ComposerSendButton")
            .semantics {
                onClick(label = resolvedLabel) {
                    onClick()
                    true
                }
            },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChatTheme.colors.accentPrimary,
            contentColor = ChatTheme.colors.buttonPrimaryTextOnAccent,
        ),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_design_ic_send),
            contentDescription = stringResource(id = R.string.stream_compose_message_composer_send_button),
        )
    }
}

/**
 * The send button with a long click action, which [FilledIconButton] does not support. It looks the same, and also
 * exposes the long click as a named accessibility action, so a screen reader user can run it without a long press.
 */
@Composable
private fun MessageComposerSendButtonWithLongClick(
    onClick: () -> Unit,
    onClickLabel: String,
    onLongClick: () -> Unit,
    onLongClickLabel: String?,
) {
    val haptics = LocalHapticFeedback.current
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(StreamTokens.spacingXs)
            .testTag("Stream_ComposerSendButton")
            .clip(CircleShape)
            .background(ChatTheme.colors.accentPrimary)
            .combinedClickable(
                onClickLabel = onClickLabel,
                role = Role.Button,
                onLongClickLabel = onLongClickLabel,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onClick = onClick,
            )
            .semantics {
                onLongClickLabel?.let { label ->
                    customActions = listOf(
                        CustomAccessibilityAction(label) {
                            currentOnLongClick()
                            true
                        },
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_design_ic_send),
            contentDescription = stringResource(id = R.string.stream_compose_message_composer_send_button),
            tint = ChatTheme.colors.buttonPrimaryTextOnAccent,
        )
    }
}

@Composable
internal fun MessageComposerSaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    onClickLabel: String? = null,
) {
    val resolvedLabel = onClickLabel ?: stringResource(R.string.stream_compose_message_composer_save_action)
    FilledIconButton(
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .padding(StreamTokens.spacingXs)
            .testTag("Stream_ComposerSaveButton")
            .semantics {
                onClick(label = resolvedLabel) {
                    onClick()
                    true
                }
            },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChatTheme.colors.accentPrimary,
            contentColor = ChatTheme.colors.buttonPrimaryTextOnAccent,
            disabledContainerColor = ChatTheme.colors.backgroundUtilityDisabled,
            disabledContentColor = ChatTheme.colors.textDisabled,
        ),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_design_ic_checkmark),
            contentDescription = stringResource(id = R.string.stream_compose_message_composer_save_button),
        )
    }
}

@Preview
@Composable
private fun MessageComposerSendButtonWithSchedulePreview() {
    ChatTheme {
        MessageComposerSendButtonWithSchedule()
    }
}

@Composable
internal fun MessageComposerSendButtonWithSchedule() {
    MessageComposerSendButton(onClick = {}, onLongClick = {}, onLongClickLabel = "Schedule message")
}
