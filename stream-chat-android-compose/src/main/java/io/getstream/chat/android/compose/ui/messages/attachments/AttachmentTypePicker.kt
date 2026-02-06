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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.util.extensions.isPollEnabled
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.previewdata.PreviewCommandData
import io.getstream.chat.android.ui.common.state.messages.MessageMode

@Composable
internal fun AttachmentTypePicker(
    channel: Channel,
    messageMode: MessageMode,
    selectedMode: AttachmentPickerMode?,
    onModeSelected: (AttachmentPickerMode) -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val modes = ChatTheme.attachmentPickerConfig.modes.filterByCapabilities(
        channel = channel,
        messageMode = messageMode,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StreamTokens.spacingMd,
                bottom = StreamTokens.spacingSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        modes.forEach { mode ->
            val isSelected = selectedMode != null &&
                selectedMode::class == mode::class
            AttachmentPickerModeInfos[mode::class]?.let { typeInfo ->
                AttachmentPickerToggleButton(
                    pickerTypeInfo = typeInfo,
                    isSelected = isSelected,
                    onClick = { onModeSelected(mode) },
                )
            }
        }
        trailingContent()
    }
    LaunchedEffect(modes) {
        modes.firstOrNull()?.let(onModeSelected)
    }
}

@Composable
internal fun AttachmentTypeSystemPicker(
    channel: Channel,
    messageMode: MessageMode,
    onModeSelected: (AttachmentPickerMode) -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val modes = ChatTheme.attachmentPickerConfig.modes.filterByCapabilities(
        channel = channel,
        messageMode = messageMode,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StreamTokens.spacingMd,
                bottom = StreamTokens.spacingSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        modes.forEach { mode ->
            AttachmentPickerModeInfos[mode::class]?.let { typeInfo ->
                AttachmentPickerButton(
                    pickerTypeInfo = typeInfo,
                    onClick = { onModeSelected(mode) },
                )
            }
        }
        trailingContent()
    }
}

@Composable
private fun AttachmentPickerToggleButton(
    pickerTypeInfo: AttachmentPickerModeInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilledIconToggleButton(
        modifier = Modifier.size(48.dp),
        checked = isSelected,
        onCheckedChange = { onClick() },
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = Color.Transparent,
            contentColor = ChatTheme.colors.buttonSecondaryText,
            checkedContainerColor = ChatTheme.colors.backgroundCoreSelected,
            checkedContentColor = ChatTheme.colors.buttonSecondaryText,
        ),
    ) {
        Icon(
            modifier = Modifier.testTag(pickerTypeInfo.testTag),
            painter = painterResource(pickerTypeInfo.icon),
            contentDescription = stringResource(pickerTypeInfo.contentDescription),
        )
    }
}

@Composable
private fun AttachmentPickerButton(
    pickerTypeInfo: AttachmentPickerModeInfo,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.size(48.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = ChatTheme.colors.buttonSecondaryText,
        ),
    ) {
        Icon(
            modifier = Modifier.testTag(pickerTypeInfo.testTag),
            painter = painterResource(pickerTypeInfo.icon),
            contentDescription = stringResource(pickerTypeInfo.contentDescription),
        )
    }
}

/**
 * Filters the list of attachment picker modes based on channel capabilities.
 * For example, [PollPickerMode] is only shown if the channel has polls enabled
 * and the message mode is [MessageMode.Normal].
 */
private fun List<AttachmentPickerMode>.filterByCapabilities(
    channel: Channel,
    messageMode: MessageMode,
): List<AttachmentPickerMode> = filter { mode ->
    when (mode) {
        is PollPickerMode -> channel.isPollEnabled() && messageMode is MessageMode.Normal
        is CommandPickerMode -> channel.config.commands.isNotEmpty()
        else -> true
    }
}

private data class AttachmentPickerModeInfo(
    @get:DrawableRes val icon: Int,
    @get:StringRes val contentDescription: Int,
    val testTag: String,
)

private val AttachmentPickerModeInfos = mapOf(
    GalleryPickerMode::class to AttachmentPickerModeInfo(
        icon = R.drawable.stream_compose_ic_attachment_media_picker,
        contentDescription = R.string.stream_compose_attachment_media_picker,
        testTag = "Stream_AttachmentPickerImagesTab",
    ),
    CameraPickerMode::class to AttachmentPickerModeInfo(
        icon = R.drawable.stream_compose_ic_attachment_camera_picker,
        contentDescription = R.string.stream_compose_attachment_camera_picker,
        testTag = "Stream_AttachmentPickerMediaCaptureTab",
    ),
    FilePickerMode::class to AttachmentPickerModeInfo(
        icon = R.drawable.stream_compose_ic_attachment_file_picker,
        contentDescription = R.string.stream_compose_attachment_file_picker,
        testTag = "Stream_AttachmentPickerFilesTab",
    ),
    PollPickerMode::class to AttachmentPickerModeInfo(
        icon = R.drawable.stream_compose_ic_attachment_polls_picker,
        contentDescription = R.string.stream_compose_attachment_polls_picker,
        testTag = "Stream_AttachmentPickerPollsTab",
    ),
    CommandPickerMode::class to AttachmentPickerModeInfo(
        icon = R.drawable.stream_compose_ic_attachment_commands_picker,
        contentDescription = R.string.stream_compose_attachment_commands_picker,
        testTag = "Stream_AttachmentPickerCommandsTab",
    ),
)

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
        selectedMode = GalleryPickerMode(),
    )
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
        selectedMode = PollPickerMode(),
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentTypePickerWithCommandsPreview() {
    ChatTheme {
        AttachmentTypePickerWithCommands()
    }
}

@Composable
internal fun AttachmentTypePickerWithCommands() {
    AttachmentTypePicker(
        channel = Channel(
            config = Config(commands = listOf(PreviewCommandData.command1)),
        ),
        messageMode = MessageMode.Normal,
        selectedMode = CommandPickerMode,
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentTypeSystemPickerPreview() {
    ChatTheme {
        AttachmentTypeSystemPicker()
    }
}

@Composable
internal fun AttachmentTypeSystemPicker() {
    AttachmentTypeSystemPicker(
        channel = Channel(),
        messageMode = MessageMode.Normal,
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentTypeSystemPickerWithPollsPreview() {
    ChatTheme {
        AttachmentTypeSystemPickerWithPolls()
    }
}

@Composable
internal fun AttachmentTypeSystemPickerWithPolls() {
    AttachmentTypeSystemPicker(
        channel = Channel(
            ownCapabilities = setOf(ChannelCapabilities.SEND_POLL),
            config = Config(pollsEnabled = true),
        ),
        messageMode = MessageMode.Normal,
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentTypeSystemPickerWithCommandsPreview() {
    ChatTheme {
        AttachmentTypeSystemPickerWithCommands()
    }
}

@Composable
internal fun AttachmentTypeSystemPickerWithCommands() {
    AttachmentTypeSystemPicker(
        channel = Channel(
            config = Config(commands = listOf(PreviewCommandData.command1)),
        ),
        messageMode = MessageMode.Normal,
    )
}
