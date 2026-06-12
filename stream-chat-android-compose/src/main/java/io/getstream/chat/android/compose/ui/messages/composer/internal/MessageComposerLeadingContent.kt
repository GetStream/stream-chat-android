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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canUploadFile
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState

@Composable
internal fun MessageComposerLeadingContent(
    messageInputState: MessageComposerState,
    isAttachmentPickerVisible: Boolean,
    onAttachmentsClick: () -> Unit,
    onAttachmentsClickLabel: String? = null,
) {
    val canSendMessage = messageInputState.canSendMessage()

    val isRecording = messageInputState.recording !is RecordingState.Idle

    val canUploadFile = messageInputState.canUploadFile()

    val isCommandActive = messageInputState.activeCommand != null

    // During slow mode the button stays visible but becomes non-interactive.
    val isInCoolDown = messageInputState.coolDownTime > 0

    val showAttachmentButton = canSendMessage && !isRecording && canUploadFile && !isCommandActive

    AnimatedContent(
        targetState = showAttachmentButton,
        contentAlignment = Alignment.Center,
    ) { visible ->
        if (visible) {
            val iconRotation by animateFloatAsState(
                targetValue = if (isAttachmentPickerVisible) OpenAttachmentPickerButtonRotation else 0f,
            )
            AttachmentPickerButton(
                isPickerVisible = isAttachmentPickerVisible,
                iconRotation = iconRotation,
                enabled = !isInCoolDown,
                onClick = onAttachmentsClick,
                onClickLabel = onAttachmentsClickLabel,
            )
        }
    }
}

@Composable
private fun AttachmentPickerButton(
    isPickerVisible: Boolean,
    iconRotation: Float,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onClickLabel: String? = null,
) {
    val pickerStateDescription = stringResource(
        if (isPickerVisible) {
            R.string.stream_compose_message_composer_attachments_expanded
        } else {
            R.string.stream_compose_message_composer_attachments_collapsed
        },
    )
    val resolvedActionLabel = onClickLabel ?: stringResource(
        if (isPickerVisible) {
            R.string.stream_compose_message_composer_attachments_close
        } else {
            R.string.stream_compose_message_composer_attachments_open
        },
    )
    FilledIconButton(
        modifier = Modifier
            .padding(end = 8.dp)
            .border(
                width = 1.dp,
                color = if (enabled) ChatTheme.colors.borderCoreDefault else ChatTheme.colors.borderUtilityDisabled,
                shape = CircleShape,
            )
            .then(
                if (ChatTheme.config.composer.floatingStyleEnabled) {
                    Modifier.shadow(6.dp, shape = CircleShape)
                } else {
                    Modifier
                },
            )
            .size(48.dp)
            .testTag("Stream_ComposerAttachmentsButton")
            .semantics {
                stateDescription = pickerStateDescription
                if (enabled) {
                    onClick(label = resolvedActionLabel) {
                        onClick()
                        true
                    }
                }
            },
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChatTheme.colors.backgroundCoreElevation1,
            disabledContainerColor = ChatTheme.colors.backgroundCoreElevation1,
            contentColor = ChatTheme.colors.buttonSecondaryText,
            disabledContentColor = ChatTheme.colors.textDisabled,
        ),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.graphicsLayer { rotationZ = iconRotation },
            painter = painterResource(id = R.drawable.stream_design_ic_plus),
            contentDescription = stringResource(R.string.stream_compose_message_composer_attachments),
        )
    }
}

private const val OpenAttachmentPickerButtonRotation = 225f
