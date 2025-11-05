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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.DefaultOnlineIndicator
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewThreadData
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator

/**
 * The basic Thread item, showing information about the Thread title, parent message, last reply and number of unread
 * replies.
 *
 * @param thread The [Thread] object holding the data to be rendered.
 * @param currentUser The currently logged [User], used for formatting the message in the thread preview.
 * @param onThreadClick Action invoked when the user clicks on the item.
 * @param modifier [Modifier] instance for general styling.
 * @param titleContent Composable rendering the title of the thread item. Defaults to a 'thread' icon and the name of
 * the channel in which the thread resides.
 * @param replyToContent Composable rendering the preview of the thread parent message. Defaults to a preview of the
 * parent message with a 'replied to:' prefix.
 * @param unreadCountContent Composable rendering the badge indicator of unread replies in a thread. Defaults to a red
 * circular badge with the unread count inside.
 * @param latestReplyContent Composable rendering the preview of the latest reply in the thread. Defaults to a content
 * composed of the reply author image, reply author name, preview of the reply text and a timestamp.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ThreadItem(
    thread: Thread,
    currentUser: User?,
    onThreadClick: (Thread) -> Unit,
    modifier: Modifier = Modifier,
    titleContent: @Composable (Channel) -> Unit = { channel ->
        ChatTheme.componentFactory.ThreadListItemTitle(thread, channel, currentUser)
    },
    replyToContent: @Composable RowScope.(parentMessage: Message) -> Unit = {
        with(ChatTheme.componentFactory) {
            ThreadListItemReplyToContent(thread)
        }
    },
    unreadCountContent: @Composable RowScope.(unreadCount: Int) -> Unit = { unreadCount ->
        with(ChatTheme.componentFactory) {
            ThreadListItemUnreadCountContent(unreadCount)
        }
    },
    latestReplyContent: @Composable (reply: Message) -> Unit = {
        ChatTheme.componentFactory.ThreadListItemLatestReplyContent(thread, currentUser)
    },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onThreadClick(thread) },
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = 8.dp, vertical = 14.dp),
    ) {
        thread.channel?.let { channel ->
            titleContent(channel)
        }
        val unreadCount = unreadCountForUser(thread, currentUser)
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            replyToContent(thread.parentMessage)
            unreadCountContent(unreadCount)
        }
        thread.latestReplies.lastOrNull()?.let { reply ->
            latestReplyContent(reply)
        }
    }
}

/**
 * Default representation of the thread title.
 *
 * @param channel The [Channel] in which the thread resides.
 * @param currentUser The currently logged [User], used for formatting the message in the thread preview.
 */
@Composable
internal fun ThreadItemTitle(
    channel: Channel,
    currentUser: User?,
) {
    val title = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_thread),
            contentDescription = null,
            tint = ChatTheme.colors.textHighEmphasis,
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

/**
 * Default representation of the parent message preview in a thread.
 *
 * @param parentMessage The parent message of the thread.
 */
@Composable
internal fun RowScope.ThreadItemReplyToContent(parentMessage: Message) {
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
}

/**
 * Default representation of the unread count badge.
 *
 * @param unreadCount The number of unread thread replies.
 */
@Composable
internal fun RowScope.ThreadItemUnreadCountContent(unreadCount: Int) {
    if (unreadCount > 0) {
        UnreadCountIndicator(
            unreadCount = unreadCount,
        )
    }
}

/**
 * Default representation of the latest reply content in a thread.
 *
 * @param thread The thread to display.
 */
@Composable
internal fun ThreadItemLatestReplyContent(
    thread: Thread,
    currentUser: User?,
) {
    val latestReply = thread.latestReplies.lastOrNull()
    if (latestReply != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            ChatTheme.componentFactory.UserAvatar(
                modifier = Modifier.size(ChatTheme.dimens.channelAvatarSize),
                user = latestReply.user,
                textStyle = ChatTheme.typography.title3Bold,
                showOnlineIndicator = latestReply.user.shouldShowOnlineIndicator(
                    userPresence = ChatTheme.userPresence,
                    currentUser = currentUser,
                ),
                onlineIndicator = {
                    DefaultOnlineIndicator(onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd)
                },
                onClick = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = latestReply.user.name,
                    style = ChatTheme.typography.bodyBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ChatTheme.colors.textHighEmphasis,
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val text = formatMessage(latestReply)
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
                        date = latestReply.updatedAt ?: latestReply.getCreatedAtOrNull(),
                    )
                }
            }
        }
    }
}

private fun unreadCountForUser(thread: Thread, user: User?) =
    thread.read
        .find { it.user.id == user?.id }
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
    ChatPreviewTheme {
        Surface {
            ThreadItem(
                thread = PreviewThreadData.thread,
                currentUser = PreviewThreadData.participant2,
                onThreadClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun DefaultThreadTitlePreview() {
    ChatPreviewTheme {
        Surface {
            ThreadItemTitle(
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
private fun DefaultUnreadCountContentPreview() {
    ChatPreviewTheme {
        Row {
            ThreadItemUnreadCountContent(unreadCount = 17)
        }
    }
}

@Composable
@Preview
private fun ThreadParentMessageContentPreview() {
    ChatPreviewTheme {
        Row {
            val parentMessage = Message(
                id = "message1",
                cid = "messaging:123",
                text = "Hey everyone, who's up for a group ride this Saturday morning?",
            )
            ThreadItemReplyToContent(parentMessage)
        }
    }
}
