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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
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
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.CommandDefaults
import io.getstream.chat.android.ui.common.R as UiCommonR

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
            MessageComposerCommandSuggestionItemCenterContent(command = it, modifier = Modifier.weight(1f))
        }
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCommandSelected(command) }
            .padding(StreamTokens.spacingSm)
            .testTag("Stream_SuggestionListGiphyButton"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent(command)

        centerContent(command)
    }
}

/**
 * Represents the default content shown at the start of the command list item.
 *
 * @param command The command to show the icon for.
 */
@Composable
internal fun DefaultCommandSuggestionItemLeadingContent(command: Command) {
    if (command.isMonochromaticIcon) {
        Icon(
            modifier = Modifier.padding(end = StreamTokens.spacingSm),
            painter = painterResource(id = command.imageRes),
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )
    } else {
        Image(
            modifier = Modifier.padding(end = StreamTokens.spacingSm),
            painter = painterResource(id = command.imageRes),
            contentDescription = null,
        )
    }
}

/**
 *  Represents the center portion of the command item, that show the user name and the user ID.
 *
 *  @param command The user to show the info for.
 *  @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultCommandSuggestionItemCenterContent(
    command: Command,
    modifier: Modifier = Modifier,
) {
    val commandDescription = LocalContext.current.getString(
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

private val Command.imageRes: Int
    @DrawableRes get() = when (name) {
        CommandDefaults.MUTE -> UiCommonR.drawable.stream_ic_command_mute
        CommandDefaults.UNMUTE -> UiCommonR.drawable.stream_ic_command_unmute
        // fallback to the 'giphy' icon for backwards compatibility
        else -> R.drawable.stream_ic_command_giphy
    }

private val Command.isMonochromaticIcon: Boolean
    get() = name == CommandDefaults.MUTE ||
        name == CommandDefaults.UNMUTE
