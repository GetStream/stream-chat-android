package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows a row of participants in the message thread, if they exist.
 *
 * @param participants List of users in the thread.
 * @param modifier Modifier for styling.
 * @param text Text of the label.
 */
@Composable
public fun ThreadParticipants(
    participants: List<User>,
    modifier: Modifier = Modifier,
    text: String,
) {
    Row(modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)) {
        Text(
            modifier = Modifier.padding(end = 4.dp),
            text = text,
            style = ChatTheme.typography.footnoteBold,
            color = ChatTheme.colors.primaryAccent
        )

        for (user in participants) {
            val painter = rememberImagePainter(data = user.image)
            Avatar(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp),
                painter = painter
            )
        }
    }
}
