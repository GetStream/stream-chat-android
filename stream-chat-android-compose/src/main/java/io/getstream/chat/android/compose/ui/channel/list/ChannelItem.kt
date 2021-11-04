package io.getstream.chat.android.compose.ui.channel.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.common.Timestamp
import io.getstream.chat.android.compose.ui.common.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastMessage
import io.getstream.chat.android.compose.ui.util.getLastMessagePreviewText
import io.getstream.chat.android.compose.ui.util.getReadStatuses

private const val UNREAD_COUNT_MANY = "99+"

/**
 * The basic channel item, that shows the channel in a list and exposes single and long click actions.
 *
 * @param channel The channel data to show.
 * @param currentUser The user that's currently logged in.
 * @param onChannelClick Handler for a single tap on an item.
 * @param onChannelLongClick Handler for a long tap on an item.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DefaultChannelItem(
    channel: Channel,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = ChatTheme.colors.appBackground)
            .combinedClickable(
                onClick = { onChannelClick(channel) },
                onLongClick = { onChannelLongClick(channel) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = ChatTheme.dimens.channelItemVerticalPadding,
                    horizontal = ChatTheme.dimens.channelItemHorizontalPadding
                ),
            verticalAlignment = CenterVertically,
        ) {
            ChannelAvatar(
                modifier = Modifier.size(ChatTheme.dimens.channelAvatarSize),
                channel = channel,
                currentUser = currentUser
            )

            Spacer(Modifier.width(8.dp))

            val lastMessage = channel.getLastMessage(currentUser)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = ChatTheme.channelNameFormatter.format(channel),
                    style = ChatTheme.typography.bodyBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ChatTheme.colors.textHighEmphasis,
                )

                val lastMessageText = channel.getLastMessagePreviewText(currentUser)

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

            if (lastMessage != null) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .wrapContentHeight()
                        .align(Bottom),
                    horizontalAlignment = Alignment.End
                ) {
                    val unreadCount = channel.unreadCount

                    if (unreadCount != null && unreadCount > 0) {
                        UnreadCountIndicator(unreadCount = unreadCount)
                    }

                    Row(verticalAlignment = CenterVertically) {
                        MessageReadStatusIcon(
                            channel = channel,
                            lastMessage = lastMessage,
                            currentUser = currentUser,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(12.dp)
                        )

                        Timestamp(date = channel.lastUpdated)
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(color = ChatTheme.colors.borders)
        )
    }
}

/**
 * Shows the unread count badge for each channel item, to showcase how many messages the user didn't read.
 *
 * @param unreadCount The number of messages the user didn't read.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UnreadCountIndicator(
    unreadCount: Int,
    modifier: Modifier = Modifier,
    color: Color = ChatTheme.colors.errorAccent,
) {
    val displayText = if (unreadCount > 99) UNREAD_COUNT_MANY else unreadCount.toString()
    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .background(shape = shape, color = color)
            .padding(horizontal = 4.dp),
        contentAlignment = Center
    ) {
        Text(
            text = displayText,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.captionBold
        )
    }
}

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
