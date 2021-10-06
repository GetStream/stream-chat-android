package io.getstream.chat.android.compose.ui.channel.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.common.Timestamp
import io.getstream.chat.android.compose.ui.common.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getDisplayName
import io.getstream.chat.android.compose.ui.util.getLastMessage
import io.getstream.chat.android.compose.ui.util.getLastMessagePreviewText
import io.getstream.chat.android.compose.ui.util.getReadStatuses

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
                .padding(vertical = 12.dp),
            verticalAlignment = CenterVertically,
        ) {
            ChannelAvatar(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp),
                channel = channel,
                currentUser = currentUser
            )

            Spacer(Modifier.width(8.dp))

            val lastMessage = channel.messages.lastOrNull()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = channel.getDisplayName(),
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
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .wrapContentHeight()
                        .align(Bottom),
                    verticalAlignment = CenterVertically,
                ) {

                    val lastMessage = channel.getLastMessage(currentUser)
                    val readStatues = channel.getReadStatuses(
                        userToIgnore = currentUser
                    )
                    val syncStatus = lastMessage?.syncStatus
                    val readCount =
                        if (lastMessage == null) 0 else readStatues.count { it.time >= lastMessage.getCreatedAtOrThrow().time }

                    val currentUserSentMessage = lastMessage?.user?.id == currentUser?.id

                    val messageIcon = when {
                        !currentUserSentMessage || readCount == 0 -> R.drawable.stream_compose_message_sent
                        currentUserSentMessage && readCount > 0 -> R.drawable.stream_compose_message_seen
                        syncStatus == SyncStatus.SYNC_NEEDED || syncStatus == SyncStatus.AWAITING_ATTACHMENTS -> R.drawable.stream_compose_ic_clock
                        syncStatus == SyncStatus.COMPLETED -> R.drawable.stream_compose_message_sent
                        else -> null
                    }

                    val iconTint =
                        if (readStatues.isNotEmpty() && readCount == readStatues.size) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis

                    if (messageIcon != null) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(12.dp),
                            painter = painterResource(id = messageIcon),
                            contentDescription = null,
                            tint = iconTint,
                        )
                    }

                    Timestamp(date = channel.lastUpdated)
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
