package io.getstream.chat.android.compose.ui.components.composer

import android.graphics.drawable.Icon
import android.inputmethodservice.Keyboard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import org.w3c.dom.Text

/**
 * Shows the options "header" for the message input component. This is based on the currently active
 * message action - [io.getstream.chat.android.common.state.Reply] or [io.getstream.chat.android.common.state.Edit].
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
    val optionImage =
        painterResource(
            id = if (activeAction is Reply) {
                R.drawable.stream_compose_ic_reply
            } else {
                R.drawable.stream_compose_ic_edit
            }
        )
    val title = stringResource(
        id = if (activeAction is Reply) {
            R.string.stream_compose_reply_to_message
        } else {
            R.string.stream_compose_edit_message
        }
    )

    Keyboard.Row(
        modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            painter = optionImage,
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
            painter = painterResource(id = R.drawable.stream_compose_ic_close),
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.textLowEmphasis,
        )
    }
}
