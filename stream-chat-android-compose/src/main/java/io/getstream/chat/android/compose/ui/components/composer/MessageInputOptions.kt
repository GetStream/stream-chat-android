package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the options "header" for the message input component. This is based on the currently active
 * message action - [Reply] or [Edit].
 *
 * @param modifier Modifier for styling.
 * @param activeAction Currently active [MessageAction].
 * @param onCancelAction Handler when the user cancels the current action.
 */
@Composable
public fun MessageInputOptions(
    activeAction: MessageAction,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val optionImage = if (activeAction is Reply) Icons.Default.Reply else Icons.Default.Edit
    val title = stringResource(
        id = if (activeAction is Reply) R.string.stream_compose_reply_to_message else R.string.stream_compose_edit_message
    )

    Row(
        modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = optionImage,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Icon(
            modifier = Modifier
                .padding(4.dp)
                .clickable(
                    onClick = onCancelAction,
                    indication = rememberRipple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            imageVector = Icons.Default.Cancel,
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.textLowEmphasis,
        )
    }
}
