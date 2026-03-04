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

package io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.previewdata.PreviewCommandData

@Composable
internal fun CommandSuggestionList(
    commands: List<Command>,
    onCommandSelected: (Command) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("Stream_CommandSuggestionList"),
    ) {
        item {
            Text(
                modifier = Modifier
                    .padding(
                        start = StreamTokens.spacingMd,
                        end = StreamTokens.spacingMd,
                        top = StreamTokens.spacingMd,
                        bottom = StreamTokens.spacingXs,
                    )
                    .fillMaxWidth(),
                text = stringResource(R.string.stream_compose_message_composer_instant_commands),
                style = ChatTheme.typography.headingSmall,
                color = ChatTheme.colors.textTertiary,
            )
        }

        items(
            items = commands,
            key = Command::name,
        ) { command ->
            ChatTheme.componentFactory.MessageComposerCommandSuggestionItem(
                command = command,
                onCommandSelected = onCommandSelected,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun CommandSuggestionListPreview() {
    ChatPreviewTheme {
        CommandSuggestionList()
    }
}

@Composable
internal fun CommandSuggestionList() {
    CommandSuggestionList(
        commands = listOf(
            PreviewCommandData.command1,
            PreviewCommandData.command2,
            PreviewCommandData.command3,
        ),
    )
}
