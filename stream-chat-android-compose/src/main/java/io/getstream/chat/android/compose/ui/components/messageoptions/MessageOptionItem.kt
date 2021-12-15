package io.getstream.chat.android.compose.ui.components.messageoptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Each option item in the column of options.
 *
 * @param option The option to show.
 * @param modifier Modifier for styling.
 * @param verticalAlignment Used to apply vertical alignment.
 * @param horizontalArrangement Used to apply horizontal arrangement.
 */
@Composable
public fun MessageOptionItem(
    option: MessageOptionItemState,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    val title = stringResource(id = option.title)

    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 16.dp),
            painter = option.iconPainter,
            tint = option.iconColor,
            contentDescription = title,
        )

        Text(
            text = title,
            style = ChatTheme.typography.body,
            color = option.titleColor
        )
    }
}

/**
 * Preview of [MessageOptionItem].
 * */
@Preview(showBackground = true, name = "MessageOptionItem Preview")
@Composable
private fun MessageOptionItemPreview() {
    ChatTheme {
        val messageOptionsState =
            defaultMessageOptionsState(
                selectedMessage = Message(),
                currentUser = User(),
                isInThread = false
            ).firstOrNull()

        if (messageOptionsState != null) {
            MessageOptionItem(option = messageOptionsState)
        }
    }
}
