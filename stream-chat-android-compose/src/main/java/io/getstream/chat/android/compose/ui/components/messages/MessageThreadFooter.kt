package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows a row of participants in the message thread, if they exist.
 *
 * @param participants List of users in the thread.
 * @param text Text of the label.
 * @param messageAlignment The alignment of the message, used for the content orientation.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageThreadFooter(
    participants: List<User>,
    text: String,
    messageAlignment: MessageAlignment,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(top = 4.dp)) {
        if (messageAlignment == MessageAlignment.Start) {
            ThreadParticipants(
                modifier = Modifier
                    .padding(end = 4.dp),
                participants = participants,
                alignment = messageAlignment
            )
        }

        Text(
            text = text,
            style = ChatTheme.typography.footnoteBold,
            color = ChatTheme.colors.primaryAccent
        )

        if (messageAlignment == MessageAlignment.End) {
            ThreadParticipants(
                modifier = Modifier
                    .padding(start = 4.dp),
                participants = participants,
                alignment = messageAlignment
            )
        }
    }
}
