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
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Default message footer, which contains either [MessageThreadFooter] or the default footer, which
 * holds the sender name and the timestamp.
 *
 * @param messageItem Message to show.
 */
@Composable
public fun MessageFooter(messageItem: MessageItemState) {
    val (message, position) = messageItem
    val hasThread = message.threadParticipants.isNotEmpty()
    val alignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)

    if (hasThread && !messageItem.isInThread) {
        val replyCount = message.replyCount
        MessageThreadFooter(
            participants = message.threadParticipants,
            messageAlignment = alignment,
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_footnote,
                replyCount,
                replyCount
            )
        )
    }

    if (position == MessageItemGroupPosition.Bottom || position == MessageItemGroupPosition.None) {
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!messageItem.isMine) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = message.user.name,
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis
                )
            } else {
                MessageReadStatusIcon(
                    modifier = Modifier.padding(end = 4.dp),
                    message = messageItem.message,
                    isMessageRead = messageItem.isMessageRead
                )
            }

            Timestamp(date = message.updatedAt ?: message.createdAt)
        }
    }
}
