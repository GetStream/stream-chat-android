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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
) {
    val hasCommandInput = messageInputState.inputValue.startsWith("/")
    val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
    val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

    val isAddButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions

    val canSendMessage = messageInputState.canSendMessage()

    val isRecording = messageInputState.recording !is RecordingState.Idle

    val canUploadFile = messageInputState.canUploadFile()

    if (canSendMessage && !isRecording && canUploadFile) {
        val iconRotation by animateFloatAsState(
            targetValue = if (isAttachmentPickerVisible) OpenAttachmentPickerButtonRotation else 0f,
        )
        FilledIconButton(
            enabled = isAddButtonEnabled,
            modifier = Modifier
                .padding(end = 8.dp)
                .border(
                    width = 1.dp,
                    color = ChatTheme.colors.borderCoreDefault,
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
                .testTag("Stream_ComposerAttachmentsButton"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = ChatTheme.colors.backgroundElevationElevation1,
                disabledContainerColor = ChatTheme.colors.backgroundElevationElevation1,
                contentColor = ChatTheme.colors.buttonSecondaryText,
                disabledContentColor = ChatTheme.colors.textDisabled,
            ),
            onClick = onAttachmentsClick,
        ) {
            Icon(
                modifier = Modifier.graphicsLayer { rotationZ = iconRotation },
                painter = painterResource(id = R.drawable.stream_compose_ic_add),
                contentDescription = stringResource(id = R.string.stream_compose_attachments),
            )
        }
    }
}

private const val OpenAttachmentPickerButtonRotation = 225f
