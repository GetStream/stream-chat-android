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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.ui.components.suggestions.commands.CommandSuggestionLazyList
import io.getstream.chat.android.compose.ui.components.suggestions.commands.DefaultCommandSuggestionListHeader
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerCommandSelect
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.previewdata.PreviewCommandData

@Composable
internal fun AttachmentCommandPicker(
    @Suppress("UNUSED_PARAMETER") pickerMode: CommandPickerMode, // Will be utilized in upcoming releases.
    commands: List<Command>,
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        DefaultCommandSuggestionListHeader()
        CommandSuggestionLazyList(
            commands = commands,
            onCommandSelected = { command ->
                onAttachmentPickerAction(AttachmentPickerCommandSelect(command))
            },
        )
    }
}

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
