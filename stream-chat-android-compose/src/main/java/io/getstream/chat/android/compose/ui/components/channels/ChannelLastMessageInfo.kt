package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastMessage

/**
 * Represents the information about the last message for the channel item, such as its read state and how many unread
 * messages the user has.
 *
 * @param channel The channel to show the info for.
 * @param currentUser The currently logged in user, used for data handling.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ChannelLastMessageInfo(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val lastMessage = channel.getLastMessage(currentUser)

    if (lastMessage != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End
        ) {
            val unreadCount = channel.unreadCount

            if (unreadCount != null && unreadCount > 0) {
                UnreadCountIndicator(
                    modifier = Modifier.padding(bottom = 4.dp),
                    unreadCount = unreadCount
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                MessageReadStatusIcon(
                    channel = channel,
                    lastMessage = lastMessage,
                    currentUser = currentUser,
                    modifier = Modifier
                        .padding(end = ChatTheme.dimens.channelItemHorizontalPadding)
                        .size(16.dp)
                )

                Timestamp(date = channel.lastUpdated)
            }
        }
    }
}
