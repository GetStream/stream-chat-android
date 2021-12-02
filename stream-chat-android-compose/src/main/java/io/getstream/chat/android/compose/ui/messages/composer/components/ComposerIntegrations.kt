package io.getstream.chat.android.compose.ui.messages.composer.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.composer.MessageInputState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Composable that represents the message composer integrations (special actions).
 *
 * Currently just shows the Attachment picker action.
 *
 * @param messageInputState The state of the input.
 * @param onAttachmentsClick Handler when the user selects attachments.
 * @param onCommandsClick Handler when the user selects commands.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultComposerIntegrations(
    messageInputState: MessageInputState,
    onAttachmentsClick: () -> Unit,
    onCommandsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp),
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_attachments),
                    contentDescription = stringResource(id = R.string.stream_compose_attachments),
                    tint = ChatTheme.colors.textLowEmphasis,
                )
            },
            onClick = onAttachmentsClick
        )

        val commandsPopupVisible = messageInputState.commandSuggestions.isNotEmpty()
        val commandsButtonEnabled = messageInputState.inputValue.isEmpty()

        val commandsButtonTint = if (commandsPopupVisible && commandsButtonEnabled) {
            ChatTheme.colors.primaryAccent
        } else if (commandsButtonEnabled) {
            ChatTheme.colors.textLowEmphasis
        } else {
            ChatTheme.colors.disabled
        }

        IconButton(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp),
            enabled = commandsButtonEnabled,
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_command),
                    contentDescription = null,
                    tint = commandsButtonTint,
                )
            },
            onClick = onCommandsClick
        )
    }
}
