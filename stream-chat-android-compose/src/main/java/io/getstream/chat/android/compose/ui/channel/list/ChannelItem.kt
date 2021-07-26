package io.getstream.chat.android.compose.ui.channel.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.Message
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.getUnreadMessagesCount
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.ui.common.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberChannelImagePainter
import java.text.SimpleDateFormat
import java.util.Date

/**
 * The basic channel item, that shows the channel in a list and exposes single and long click actions.
 *
 * @param modifier - For special styling, like theming.
 * @param item - The channel data to show.
 * @param onChannelClick - Handler for a single tap on an item.
 * @param onChannelLongClick - Handler for a long tap on an item.
 * @param modifier - Modifier for styling.
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DefaultChannelItem(
    item: Channel,
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
                onClick = { onChannelClick(item) },
                onLongClick = { onChannelLongClick(item) },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalAlignment = CenterVertically,
    ) {
        val imagePainter = rememberChannelImagePainter(channel = item, currentUser = currentUser)

        Avatar(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp),
            painter = imagePainter,
        )

        Spacer(Modifier.width(8.dp))

        val lastMessage =
            item.messages.lastOrNull { it.deletedAt == null && it.attachments.isEmpty() }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp),
        ) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Text(
                text = lastMessage?.text ?: "No message",
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }

        if (lastMessage != null) {
            Row(
                modifier = Modifier
                    .padding(end = 4.dp, bottom = 4.dp)
                    .wrapContentHeight()
                    .align(Bottom),
                verticalAlignment = CenterVertically,
            ) {
                val messageIcon =
                    if (item.getUnreadMessagesCount("") == 0) Icons.Default.Message else Icons.Outlined.Message

                Icon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(12.dp),
                    imageVector = messageIcon,
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                ) // TODO seen/not

                Text(
                    text = SimpleDateFormat.getTimeInstance().format(item.lastUpdated ?: Date()),
                    fontSize = 14.sp,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
        }
    }
}
