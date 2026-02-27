/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatarStack
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.isOneToOne
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewThreadData
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator

/**
 * The basic Thread item, showing information about the thread title, parent message, latest reply
 * and number of unread replies.
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
    val borderColor = ChatTheme.colors.borderCoreSubtle
    val unreadCount = unreadCountForUser(thread, currentUser)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(StreamTokens.spacing2xs)
            .clip(RoundedCornerShape(StreamTokens.radiusLg))
            .combinedClickable(
                onClick = { onThreadClick(thread) },
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(all = StreamTokens.spacingSm),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
        verticalAlignment = Alignment.Top,
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(AvatarSize.ExtraLarge),
            user = thread.parentMessage.user,
            showIndicator = thread.parentMessage.user.shouldShowOnlineIndicator(
                userPresence = ChatTheme.userPresence,
                currentUser = currentUser,
            ),
            showBorder = false,
        )
        ThreadItemContentContainer(
            modifier = Modifier.weight(1f),
            thread = thread,
            currentUser = currentUser,
        )
        if (unreadCount > 0) {
            UnreadCountIndicator(unreadCount)
        }
    }
}

/**
 * Displays the channel name where the thread resides.
 *
 * @param channel The [Channel] hosting the thread.
 * @param currentUser The currently logged [User], used for formatting the channel name.
 */
@Composable
internal fun ThreadItemTitle(
    channel: Channel,
    currentUser: User?,
) {
    val title = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
    Text(
        text = title,
        color = ChatTheme.colors.textTertiary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = ChatTheme.typography.captionEmphasis,
    )
}

/**
 * Displays a single-line preview of the thread's parent message.
 * Deleted messages are shown in italic using a localised "message deleted" label.
 *
 * @param thread The [Thread] to render the parent message for.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun ThreadItemParentMessage(thread: Thread, currentUser: User?) {
    val isOneToOneChannel = thread.channel?.isOneToOne(currentUser) ?: false
    val message = thread.parentMessage
    val formatter = ChatTheme.messagePreviewFormatter
    val text = remember(message, currentUser, isOneToOneChannel, formatter) {
        formatter.formatMessagePreview(message, currentUser, isOneToOneChannel)
    }
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        color = ChatTheme.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = ChatTheme.typography.bodyDefault,
        inlineContent = ChatTheme.messagePreviewIconFactory.createPreviewIcons(),
    )
}

/**
 * Displays a horizontal stack of participant avatars for the thread.
 *
 * @param participants The [User]s whose avatars are shown, typically the most recent thread
 *   participants (up to 3), in newest-first order so the latest replier sits on top.
 */
@Composable
internal fun ThreadItemParticipants(participants: List<User>) {
    UserAvatarStack(
        overlap = StreamTokens.spacingXs,
        users = participants,
        avatarSize = AvatarSize.Small,
        showBorder = true,
    )
}

/**
 * Displays the reply count label for a thread (e.g. "5 replies").
 *
 * @param replyCount The total number of replies in the thread.
 */
@Composable
internal fun ThreadItemReplyCount(replyCount: Int) {
    Text(
        text = pluralStringResource(
            id = R.plurals.stream_compose_thread_list_item_reply_count,
            count = replyCount,
            replyCount,
        ),
        style = ChatTheme.typography.captionEmphasis,
        color = ChatTheme.colors.chatTextLink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Displays the formatted timestamp for when the thread was last updated.
 *
 * @param thread The [Thread] whose [Thread.updatedAt] is formatted and displayed.
 * @see ThreadTimestampFormatter
 */
@Composable
internal fun ThreadItemTimestamp(thread: Thread) {
    val updatedAt = thread.updatedAt
    val context = LocalContext.current
    val timestamp = remember(updatedAt, context) {
        ThreadTimestampFormatter.format(updatedAt, context)
    }
    Text(
        text = timestamp,
        style = ChatTheme.typography.captionDefault,
        color = ChatTheme.colors.chatTextTimestamp,
    )
}

/**
 * Container holding the thread header ([ThreadItemTitle] and [ThreadItemParentMessage]) and the
 * [ThreadRepliesFooter], filling the available horizontal space between the avatar and the
 * notification badge.
 *
 * @param thread The [Thread] to display.
 * @param currentUser The currently logged [User].
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ThreadItemContentContainer(
    thread: Thread,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Column(
            modifier = Modifier.padding(vertical = StreamTokens.spacing3xs),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            thread.channel?.let { channel ->
                ThreadItemTitle(channel, currentUser)
            }
            ThreadItemParentMessage(thread, currentUser)
        }
        ThreadRepliesFooter(thread)
    }
}

/**
 * Footer row inside a thread item showing [ThreadItemParticipants], [ThreadItemReplyCount],
 * and [ThreadItemTimestamp].
 *
 * @param thread The [Thread] to display.
 */
@Composable
internal fun ThreadRepliesFooter(thread: Thread) {
    val latestReply = thread.latestReplies.lastOrNull() ?: return
    // The thread author will always be a thread participant, even if he didn't reply to the thread.
    // Note: Because we don't get all replies (or participants) in the response, we can not reliably know
    // whether the thread author has a reply in the thread
    // Small UI improvement is to ensure we only show the actual replier if we have only one reply.
    val participants = if (thread.replyCount == 1) {
        listOf(latestReply.user)
    } else {
        thread.threadParticipants
            .map { it.user }
            .ifEmpty { listOfNotNull(latestReply.user) }
            .take(MaxParticipantCount)
            .reversed()
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThreadItemParticipants(participants)
        ThreadItemReplyCount(thread.replyCount)
        ThreadItemTimestamp(thread)
    }
}

private const val MaxParticipantCount = 3

private fun unreadCountForUser(thread: Thread, user: User?) =
    thread.read
        .find { it.user.id == user?.id }
        ?.unreadMessages
        ?: 0

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
