/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Command

/**
 * Represents the command suggestion item in the command suggestion list popup.
 *
 * @param command The command to display.
 * @param modifier Modifier for styling.
 * @param onCommandSelected Handler when the user taps on an item.
 * @param leadingContent Customizable composable function that represents the leading content of a command item.
 * @param centerContent Customizable composable function that represents the center content of a command item.
 */
@Composable
public fun CommandSuggestionItem(
    command: Command,
    modifier: Modifier = Modifier,
    onCommandSelected: (Command) -> Unit = {},
    leadingContent: @Composable RowScope.(Command) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerCommandSuggestionItemLeadingContent(command = it)
        }
    },
    centerContent: @Composable RowScope.(Command) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerCommandSuggestionItemCenterContent(command = it, modifier = Modifier)
        }
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onCommandSelected(command) }
            .padding(
                vertical = ChatTheme.dimens.commandSuggestionItemVerticalPadding,
                horizontal = ChatTheme.dimens.commandSuggestionItemHorizontalPadding,
            )
            .testTag("Stream_SuggestionListGiphyButton"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent(command)

        centerContent(command)
    }
}

/**
 * Represents the default content shown at the start of the command list item.
 */
@Composable
internal fun DefaultCommandSuggestionItemLeadingContent() {
    Image(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(ChatTheme.dimens.commandSuggestionItemIconSize),
        painter = painterResource(id = R.drawable.stream_compose_ic_giphy),
        contentDescription = null,
    )
}

/**
 *  Represents the center portion of the command item, that show the user name and the user ID.
 *
 *  @param command The user to show the info for.
 *  @param modifier Modifier for styling.
 */
@Composable
internal fun RowScope.DefaultCommandSuggestionItemCenterContent(
    command: Command,
    modifier: Modifier = Modifier,
) {
    val commandDescription = LocalContext.current.getString(
        R.string.stream_compose_message_composer_command_template,
        command.name,
        command.args,
    )

    Row(
        modifier = modifier
            .weight(1f)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = command.name.replaceFirstChar(Char::uppercase),
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = commandDescription,
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
