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

package io.getstream.chat.android.compose.ui.messages.attachments

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Commands
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.util.extensions.isPollEnabled
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.ui.common.state.messages.MessageMode

@Composable
internal fun AttachmentTypePicker(
    channel: Channel,
    messageMode: MessageMode,
    selectedAttachmentsPickerMode: AttachmentsPickerMode,
    onPickerTypeClick: (Int, AttachmentsPickerMode) -> Unit = { _, _ -> },
    content: @Composable (AttachmentsPickerMode) -> Unit,
) {
    val attachmentsPickerModes = remember(channel, messageMode) {
        AttachmentsPickerModes.filter { attachmentsPickerMode ->
            attachmentsPickerMode.isModeVisible(
                channel = channel,
                messageMode = messageMode,
            )
        }
    }

    Row(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacingMd,
                bottom = StreamTokens.spacingSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        attachmentsPickerModes.forEachIndexed { index, attachmentsPickerMode ->

            val isSelected = attachmentsPickerMode == selectedAttachmentsPickerMode

            AttachmentPickerTypeInfos[attachmentsPickerMode]?.let { typeInfo ->

                AttachmentPickerToggleButton(
                    pickerTypeInfo = typeInfo,
                    isSelected = isSelected,
                    onClick = { onPickerTypeClick(index, attachmentsPickerMode) },
                )
            }
        }
    }

    content(selectedAttachmentsPickerMode)
}

@Composable
private fun AttachmentPickerToggleButton(
    pickerTypeInfo: AttachmentPickerTypeInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilledIconToggleButton(
        modifier = Modifier.size(48.dp),
        checked = isSelected,
        onCheckedChange = { onClick() },
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = Color.Transparent,
            contentColor = ChatTheme.colors.buttonStyleGhostTextSecondary,
            checkedContainerColor = ChatTheme.colors.backgroundCoreSelected,
            checkedContentColor = ChatTheme.colors.buttonStyleGhostTextSecondary,
        ),
    ) {
        Icon(
            modifier = Modifier.testTag(pickerTypeInfo.testTag),
            painter = painterResource(pickerTypeInfo.icon),
            contentDescription = stringResource(pickerTypeInfo.contentDescription),
        )
    }
}

private fun AttachmentsPickerMode.isModeVisible(
    channel: Channel,
    messageMode: MessageMode,
) = when (this) {
    is Poll -> channel.isPollEnabled() && messageMode is MessageMode.Normal
    else -> true
}

private data class AttachmentPickerTypeInfo(
    @get:StringRes val icon: Int,
    @get:StringRes val contentDescription: Int,
    val testTag: String,
)

private val AttachmentPickerTypeInfos = mapOf(
    Images to AttachmentPickerTypeInfo(
        icon = R.drawable.stream_compose_ic_attachment_media_picker,
        contentDescription = R.string.stream_compose_attachment_media_picker,
        testTag = "Stream_AttachmentPickerImagesTab",
    ),
    MediaCapture to AttachmentPickerTypeInfo(
        icon = R.drawable.stream_compose_ic_attachment_camera_picker,
        contentDescription = R.string.stream_compose_attachment_camera_picker,
        testTag = "Stream_AttachmentPickerMediaCaptureTab",
    ),
    Files to AttachmentPickerTypeInfo(
        icon = R.drawable.stream_compose_ic_attachment_file_picker,
        contentDescription = R.string.stream_compose_attachment_file_picker,
        testTag = "Stream_AttachmentPickerFilesTab",
    ),
    Poll to AttachmentPickerTypeInfo(
        icon = R.drawable.stream_compose_ic_attachment_polls_picker,
        contentDescription = R.string.stream_compose_attachment_polls_picker,
        testTag = "Stream_AttachmentPickerPollsTab",
    ),
    Commands to AttachmentPickerTypeInfo(
        icon = R.drawable.stream_compose_ic_attachment_commands_picker,
        contentDescription = R.string.stream_compose_attachment_commands_picker,
        testTag = "Stream_AttachmentPickerCommandsTab",
    ),
)

private val AttachmentsPickerModes = AttachmentPickerTypeInfos.keys.toList()

@Preview(showBackground = true)
@Composable
private fun AttachmentTypePickerPreview() {
    ChatTheme {
        AttachmentTypePicker()
    }
}

@Composable
internal fun AttachmentTypePicker() {
    AttachmentTypePicker(
        channel = Channel(),
        messageMode = MessageMode.Normal,
        selectedAttachmentsPickerMode = Images,
    ) {}
}

@Preview(showBackground = true)
@Composable
private fun AttachmentTypePickerWithPollsPreview() {
    ChatTheme {
        AttachmentTypePickerWithPolls()
    }
}

@Composable
internal fun AttachmentTypePickerWithPolls() {
    AttachmentTypePicker(
        channel = Channel(
            ownCapabilities = setOf(ChannelCapabilities.SEND_POLL),
            config = Config(pollsEnabled = true),
        ),
        messageMode = MessageMode.Normal,
        selectedAttachmentsPickerMode = Poll,
    ) {}
}
