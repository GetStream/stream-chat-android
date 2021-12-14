package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastMessage

/**
 * Represents the details portion of the channel item, that shows the channel display name and the last message text
 * preview.
 *
 * @param channel The channel to show the info for.
 * @param isMuted If the channel is muted for the current user.
 * @param currentUser The currently logged in user, used for data handling.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ChannelDetails(
    channel: Channel,
    isMuted: Boolean,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        val channelName: (@Composable (modifier: Modifier) -> Unit) = @Composable {
            Text(
                modifier = it,
                text = ChatTheme.channelNameFormatter.formatChannelName(channel),
                style = ChatTheme.typography.bodyBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        if (isMuted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                channelName(Modifier.weight(weight = 1f, fill = false))

                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_muted),
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                )
            }
        } else {
            channelName(Modifier)
        }

        val lastMessageText = channel.getLastMessage(currentUser)?.let { lastMessage ->
            ChatTheme.messagePreviewFormatter.formatMessagePreview(lastMessage, currentUser)
        } ?: AnnotatedString("")

        if (lastMessageText.isNotEmpty()) {
            Text(
                text = lastMessageText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}
