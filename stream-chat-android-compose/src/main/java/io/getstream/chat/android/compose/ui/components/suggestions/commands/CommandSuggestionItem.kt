package io.getstream.chat.android.compose.ui.components.suggestions.commands

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the default command suggestion item in the command suggestion list popup.
 *
 * @param command The command to display.
 * @param modifier Modifier for styling.
 * @param onCommandSelected Handler when the user taps on an item.
 * @param leadingContent Customizable composable function that represents the leading content of a command item.
 * @param detailsContent Customizable composable function that represents the details content of a command item.
 */
@Composable
public fun DefaultCommandSuggestionItem(
    command: Command,
    modifier: Modifier = Modifier,
    onCommandSelected: (Command) -> Unit = {},
    leadingContent: @Composable RowScope.(Command) -> Unit = {
        DefaultCommandSuggestionItemLeadingContent(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(ChatTheme.dimens.commandSuggestionItemIconSize)
        )
    },
    detailsContent: @Composable RowScope.(Command) -> Unit = {
        DefaultCommandSuggestionItemDetailsContent(
            command = it,
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
        )
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = { onCommandSelected(command) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(
                vertical = ChatTheme.dimens.commandSuggestionItemVerticalPadding,
                horizontal = ChatTheme.dimens.commandSuggestionItemHorizontalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent(command)

        detailsContent(command)
    }
}

/**
 * Represents the default content shown at the start of the command list item.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun DefaultCommandSuggestionItemLeadingContent(
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = painterResource(id = R.drawable.stream_compose_ic_giphy),
        contentDescription = null
    )
}

/**
 *  Represents the details portion of the command item, that show the user name and the user ID.
 *
 *  @param command The user to show the info for.
 *  @param modifier Modifier for styling.
 */
@Composable
public fun DefaultCommandSuggestionItemDetailsContent(
    command: Command,
    modifier: Modifier = Modifier,
) {
    val commandDescription = LocalContext.current.getString(
        R.string.stream_compose_message_composer_command_template,
        command.name,
        command.args
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = command.name.replaceFirstChar(Char::uppercase),
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = commandDescription,
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
