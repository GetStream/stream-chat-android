package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Default message footer, which contains either [ThreadParticipants] or the default footer, which
 * holds the sender name and the timestamp.
 *
 * @param messageItem Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageFooter(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
) {
    val (message, position) = messageItem
    val hasThread = message.threadParticipants.isNotEmpty()

    if (hasThread && !messageItem.isInThread) {
        val replyCount = message.replyCount
        ThreadParticipants(
            modifier = modifier,
            participants = message.threadParticipants,
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_footnote,
                replyCount,
                replyCount
            )
        )
    } else if (!hasThread && (position == MessageItemGroupPosition.Bottom || position == MessageItemGroupPosition.None)) {
        Row(
            modifier = modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!messageItem.isMine) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = message.user.name,
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis
                )
            }

            Timestamp(date = message.updatedAt ?: message.createdAt)
        }
    }
}
