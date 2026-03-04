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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.R.string.stream_compose_message_composer_instant_commands
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.CommandDefaults
import io.getstream.chat.android.previewdata.PreviewCommandData
import io.getstream.chat.android.ui.common.R as UiCommonR

@Composable
internal fun AttachmentCommandPicker(
    @Suppress("UNUSED_PARAMETER") pickerMode: CommandPickerMode, // Will be utilized in upcoming releases.
    commands: List<Command>,
    onCommandSelected: (Command) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier
                .padding(
                    start = StreamTokens.spacingMd,
                    end = StreamTokens.spacingMd,
                    top = StreamTokens.spacingXs,
                    bottom = StreamTokens.spacingMd,
                )
                .fillMaxWidth(),
            text = stringResource(stream_compose_message_composer_instant_commands),
            style = ChatTheme.typography.headingSmall,
            color = ChatTheme.colors.textPrimary,
        )
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(
                items = commands,
                key = Command::name,
            ) { command ->
                CommandItem(
                    command = command,
                    onCommandSelected = onCommandSelected,
                )
            }
        }
    }
}

@Composable
private fun CommandItem(
    command: Command,
    modifier: Modifier = Modifier,
    onCommandSelected: (Command) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCommandSelected(command) }
            .padding(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (command.isPolychromaticIcon) {
            Image(
                modifier = Modifier.padding(end = StreamTokens.spacingSm),
                painter = painterResource(id = command.imageRes),
                contentDescription = null,
            )
        } else {
            Icon(
                modifier = Modifier.padding(end = StreamTokens.spacingSm),
                painter = painterResource(id = command.imageRes),
                contentDescription = null,
                tint = ChatTheme.colors.textSecondary,
            )
        }

        val commandDescription = stringResource(
            R.string.stream_compose_message_composer_command_template,
            command.name,
            command.args,
        )

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
        ) {
            Text(
                modifier = Modifier.width(80.dp),
                text = command.name.replaceFirstChar(Char::uppercase),
                style = ChatTheme.typography.bodyEmphasis,
                color = ChatTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = commandDescription,
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val Command.imageRes: Int
    @DrawableRes get() = when (name) {
        CommandDefaults.MUTE -> UiCommonR.drawable.stream_ic_command_mute
        CommandDefaults.UNMUTE -> UiCommonR.drawable.stream_ic_command_unmute
        CommandDefaults.BAN -> UiCommonR.drawable.stream_ic_command_ban
        CommandDefaults.UNBAN -> UiCommonR.drawable.stream_ic_command_unban
        // fallback to the 'giphy' icon for backwards compatibility
        else -> R.drawable.stream_ic_command_giphy
    }

private val Command.isPolychromaticIcon: Boolean get() = name == CommandDefaults.GIPHY

@Preview(showBackground = true)
@Composable
private fun AttachmentCommandPickerPreview() {
    ChatTheme {
        AttachmentCommandPicker()
    }
}

@Composable
internal fun AttachmentCommandPicker() {
    AttachmentCommandPicker(
        pickerMode = CommandPickerMode,
        commands = listOf(
            PreviewCommandData.command1,
            PreviewCommandData.command2,
            PreviewCommandData.command3,
        ),
    )
}
