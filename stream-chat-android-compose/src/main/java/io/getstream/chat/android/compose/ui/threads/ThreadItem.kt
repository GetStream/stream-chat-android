/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * The basic Thread item, showing information about the Thread title, parent message, last reply and number of unread
 * replies.
 *
 * @param thread The [Thread] object holding the data to be rendered.
 * @param currentUser The currently logged [User], used for formatting the message in the thread preview.
 * @param onThreadClick Action invoked when the user clicks on the item.
 * @param modifier [Modifier] instance for general styling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ThreadItem(
    thread: Thread,
    currentUser: User?,
    onThreadClick: (Thread) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onThreadClick(thread) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = 8.dp, vertical = 14.dp),
    ) {
        thread.channel?.let { channel ->
            ThreadTitle(channel, currentUser)
        }
        thread.parentMessage?.let { parentMessage ->
            val unreadCount = unreadCountForUser(thread, currentUser)
            ParentMessageContent(
                parentMessage = parentMessage,
                unreadCount = unreadCount,
            )
        }
        thread.latestReplies.lastOrNull()?.let { reply ->
            Spacer(modifier = Modifier.height(8.dp))
            LatestReplyContent(reply = reply)
        }
    }
}

@Composable
private fun ThreadTitle(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val title = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
    Row(modifier = modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_thread),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.bodyBold,
        )
    }
}

@Composable
private fun ParentMessageContent(
    parentMessage: Message,
    unreadCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val prefix = stringResource(id = R.string.stream_compose_replied_to)
        val text = formatMessage(parentMessage)
        Text(
            modifier = Modifier.weight(1f),
            text = "$prefix$text",
            fontSize = 12.sp,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.body,
        )
        if (unreadCount > 0) {
            UnreadCountIndicator(
                unreadCount = unreadCount,
            )
        }
    }
}

@Composable
private fun LatestReplyContent(reply: Message) {
    Row(modifier = Modifier.fillMaxWidth()) {
        UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.channelAvatarSize),
            user = reply.user,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = reply.user.name,
                style = ChatTheme.typography.bodyBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val text = formatMessage(reply)
                Text(
                    modifier = Modifier.weight(1f),
                    text = text,
                    maxLines = 1,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    style = ChatTheme.typography.body,
                    color = ChatTheme.colors.textLowEmphasis,
                )
                Timestamp(
                    modifier = Modifier.padding(start = 8.dp),
                    date = reply.updatedAt,
                )
            }
        }
    }
}

private fun unreadCountForUser(thread: Thread, user: User?) =
    thread.read
        ?.find { it.user.id == user?.id }
        ?.unreadMessages
        ?: 0

@Composable
private fun formatMessage(message: Message) =
    if (message.isDeleted()) {
        buildAnnotatedString {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(stringResource(id = R.string.stream_ui_message_list_message_deleted))
            }
        }
    } else {
        ChatTheme.messagePreviewFormatter.formatMessagePreview(message, null)
    }

@Composable
@Preview
private fun ThreadItemPreview() {
    ChatTheme {
        Surface {
            val user1 = User(id = "uid1", name = "User 1")
            val user2 = User(id = "uid2", name = "User 2")
            val thread = Thread(
                activeParticipantCount = 2,
                cid = "cid",
                channel = Channel(),
                parentMessageId = "pmid1",
                parentMessage = Message(
                    id = "pmid1",
                    text = "Hey everyone, who's up for a group ride this Saturday morning?",
                ),
                createdByUserId = "uid2",
                createdBy = user2,
                replyCount = 3,
                participantCount = 2,
                threadParticipants = listOf(user1, user2),
                lastMessageAt = Date(),
                createdAt = Date(),
                updatedAt = null,
                deletedAt = null,
                title = "Group ride preparation and discussion",
                latestReplies = listOf(
                    Message(id = "mid1", text = "See you all there, stay safe on the roads!", user = user1),
                ),
                read = listOf(
                    ChannelUserRead(
                        user = user2,
                        lastReceivedEventDate = Date(),
                        unreadMessages = 3,
                        lastRead = Date(),
                        lastReadMessageId = null,
                    ),
                ),
            )
            ThreadItem(
                thread = thread,
                currentUser = user2,
                onThreadClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun ThreadTitlePreview() {
    ChatTheme {
        Surface {
            ThreadTitle(
                channel = Channel(
                    id = "messaging:123",
                    type = "messaging",
                    name = "Group ride preparation and discussion",
                ),
                currentUser = null,
            )
        }
    }
}

@Composable
@Preview
private fun ThreadParentMessageContentPreview() {
    ChatTheme {
        Surface {
            val parentMessage = Message(
                id = "message1",
                cid = "messaging:123",
                text = "Hey everyone, who's up for a group ride this Saturday morning?",
            )
            val unreadCount = 2
            ParentMessageContent(parentMessage, unreadCount)
        }
    }
}
