package io.getstream.chat.android.compose.ui.channel.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.getUnreadMessagesCount
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.common.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getDisplayName
import io.getstream.chat.android.compose.ui.util.rememberChannelImagePainter
import java.text.SimpleDateFormat
import java.util.Date

/**
 * The basic channel item, that shows the channel in a list and exposes single and long click actions.
 *
 * @param modifier - For special styling, like theming.
 * @param channel - The channel data to show.
 * @param onChannelClick - Handler for a single tap on an item.
 * @param onChannelLongClick - Handler for a long tap on an item.
 * @param modifier - Modifier for styling.
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DefaultChannelItem(
    channel: Channel,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(top = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = { onChannelClick(channel) },
                onLongClick = { onChannelLongClick(channel) },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalAlignment = CenterVertically,
    ) {
        val imagePainter = rememberChannelImagePainter(channel = channel, currentUser = currentUser)

        Box(
            Modifier
                .padding(start = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ChatTheme.colors.borders)
                    .size(36.dp)
            )
            Avatar(
                modifier = Modifier.size(36.dp),
                painter = imagePainter,
            )
        }

        Spacer(Modifier.width(8.dp))

        val lastMessage = channel.messages.lastOrNull()

        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
        ) {
            Text(
                text = channel.getDisplayName(),
                style = ChatTheme.typography.bodyBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )

            val lastMessageText = lastMessage?.text?.takeIf { it.isNotBlank() }
            if (lastMessageText != null) {
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
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                    .wrapContentHeight()
                    .align(Bottom),
                verticalAlignment = CenterVertically,
            ) {
                val seenMessage = channel.getUnreadMessagesCount(currentUser?.id ?: "") == 0

                val messageIcon =
                    if (seenMessage) R.drawable.stream_compose_message_seen else R.drawable.stream_compose_message_not_seen

                Icon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(12.dp),
                    painter = painterResource(id = messageIcon),
                    contentDescription = null,
                    tint = if (seenMessage) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
                )

                Text(
                    text = SimpleDateFormat.getTimeInstance().format(channel.lastUpdated ?: Date()),
                    fontSize = 14.sp,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
        }
    }
}
