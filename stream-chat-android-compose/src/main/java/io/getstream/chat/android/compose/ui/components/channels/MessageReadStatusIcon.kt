package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getReadStatuses

/**
 * Shows the last message read status for each channel item based on the given information about the channel, the last
 * message and the current user.
 *
 * @param channel The channel to show the read status for.
 * @param lastMessage The last message in the channel.
 * @param currentUser Currently logged in user.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageReadStatusIcon(
    channel: Channel,
    lastMessage: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val readStatues = channel.getReadStatuses(userToIgnore = currentUser)
    val syncStatus = lastMessage.syncStatus
    val currentUserSentMessage = lastMessage.user.id == currentUser?.id
    val readCount = readStatues.count { it.time >= lastMessage.getCreatedAtOrThrow().time }

    val messageIcon = when {
        currentUserSentMessage && readCount == 0 -> R.drawable.stream_compose_message_sent
        !currentUserSentMessage || readCount != 0 -> R.drawable.stream_compose_message_seen
        syncStatus == SyncStatus.SYNC_NEEDED || syncStatus == SyncStatus.AWAITING_ATTACHMENTS -> R.drawable.stream_compose_ic_clock
        syncStatus == SyncStatus.COMPLETED -> R.drawable.stream_compose_message_sent
        else -> null
    }

    if (messageIcon != null) {
        val iconTint =
            if (!currentUserSentMessage || (readStatues.isNotEmpty() && readCount == readStatues.size)) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis

        Icon(
            modifier = modifier,
            painter = painterResource(id = messageIcon),
            contentDescription = null,
            tint = iconTint,
        )
    }
}
