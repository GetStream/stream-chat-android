package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Date

/**
 * Shows the content that lets the user know that only they can see the message.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun OwnedMessageVisibilityContent(
    message: Message,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(12.dp),
            imageVector = Icons.Default.RemoveRedEye, // TODO replace with an icon from design
            contentDescription = null
        )

        Text(
            text = stringResource(id = R.string.stream_compose_only_visible_to_you),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textHighEmphasis
        )

        Timestamp(
            modifier = Modifier.padding(8.dp),
            date = message.updatedAt ?: message.createdAt ?: Date()
        )
    }
}
