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

package io.getstream.chat.android.compose.ui.components.suggestions.commands

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.suggestions.SuggestionList
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Command

/**
 * Represents the command suggestion list popup.
 *
 * @param commands The list of commands to be displayed in the popup.
 * @param modifier Modifier for styling.
 * @param onCommandSelected Handler when the user taps on an item.
 * @param itemContent Customizable composable function that represents a single command item.
 */
@Composable
public fun CommandSuggestionList(
    commands: List<Command>,
    modifier: Modifier = Modifier,
    onCommandSelected: (Command) -> Unit = {},
    itemContent: @Composable (Command) -> Unit = { command ->
        ChatTheme.componentFactory.MessageComposerCommandSuggestionItem(
            command = command,
            onCommandSelected = onCommandSelected,
        )
    },
) {
    SuggestionList(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.suggestionListMaxHeight)
            .padding(ChatTheme.dimens.suggestionListPadding)
            .testTag("Stream_SuggestionList"),
        headerContent = { DefaultCommandSuggestionListHeader() },
    ) {
        CommandSuggestionLazyList(
            commands = commands,
            onCommandSelected = onCommandSelected,
            itemContent = itemContent,
        )
    }
}

@Composable
internal fun DefaultCommandSuggestionListHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(24.dp)
                .testTag("Stream_SuggestionListTitle"),
            painter = painterResource(id = R.drawable.stream_compose_ic_command),
            tint = ChatTheme.colors.primaryAccent,
            contentDescription = null,
        )
        Text(
            text = stringResource(id = R.string.stream_compose_message_composer_instant_commands),
            style = ChatTheme.typography.body,
            maxLines = 1,
            color = ChatTheme.colors.textLowEmphasis,
        )
    }
}

@Composable
internal fun CommandSuggestionLazyList(
    commands: List<Command>,
    onCommandSelected: (Command) -> Unit = {},
    itemContent: @Composable (Command) -> Unit = { command ->
        ChatTheme.componentFactory.MessageComposerCommandSuggestionItem(
            command = command,
            onCommandSelected = onCommandSelected,
        )
    },
) {
    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        items(
            items = commands,
            key = Command::name,
        ) { command ->
            itemContent(command)
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
    Column {
        DefaultCommandSuggestionListHeader()
        CommandSuggestionLazyList(
            commands = listOf(
                Command(
                    name = "giphy",
                    description = "Post a random gif to the channel",
                    args = "[text]",
                    set = "fun_set",
                ),
                Command(
                    name = "mute",
                    description = "Mute a user",
                    args = "[@username]",
                    set = "moderation_set",
                ),
                Command(
                    name = "unmute",
                    description = "Unmute a user",
                    args = "[@username]",
                    set = "moderation_set",
                ),
            ),
        )
    }
}
