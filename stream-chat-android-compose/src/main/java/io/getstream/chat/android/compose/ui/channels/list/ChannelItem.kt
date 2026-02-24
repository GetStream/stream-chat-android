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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.TypingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.getLastMessage
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewChannelUserRead
import io.getstream.chat.android.previewdata.PreviewUserData
import java.util.Date

/**
 * The basic channel item, that shows the channel in a list and exposes single and long click actions.
 *
 * @param channelItem The channel data to show.
 * @param currentUser The user that's currently logged in.
 * @param onChannelClick Handler for a single tap on an item.
 * @param onChannelLongClick Handler for a long tap on an item.
 * @param modifier Modifier for styling.
 * @param leadingContent Customizable composable function that represents the leading content of a channel item, usually
 * the avatar that holds an image of the channel or its members.
 * @param centerContent Customizable composable function that represents the center content of a channel item, usually
 * holding information about its name and the last message.
 * @param trailingContent Customizable composable function that represents the trailing content of the a channel item,
 * usually information about the last message and the number of unread messages.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ChannelItem(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelItemLeadingContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }
    },
    centerContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelItemCenterContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }
    },
    trailingContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelItemTrailingContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }
    },
) {
    val channel = channelItem.channel
    val description = stringResource(id = R.string.stream_compose_cd_channel_item)

    Column(
        modifier = modifier
            .testTag("Stream_ChannelItem")
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics { contentDescription = description }
            .combinedClickable(
                onClick = { onChannelClick(channel) },
                onLongClick = { onChannelLongClick(channel) },
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent(channelItem)

            centerContent(channelItem)

            trailingContent(channelItem)
        }
    }
}

/**
 * Represents the default leading content of [ChannelItem], that shows the channel avatar.
 *
 * @param channelItem The channel to show the avatar of.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultChannelItemLeadingContent(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
) {
    ChatTheme.componentFactory.ChannelAvatar(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacingXs,
                end = 4.dp,
                top = StreamTokens.spacingSm,
                bottom = StreamTokens.spacingSm,
            )
            .size(40.dp),
        channel = channelItem.channel,
        currentUser = currentUser,
        showIndicator = false,
        showBorder = false,
    )
}

/**
 * Represents the center portion of [ChannelItem], that shows the channel display name
 * and the last message text preview.
 *
 * @param channelItemState The channel to show the info for.
 * @param currentUser The currently logged in user, used for data handling.
 */
@Composable
internal fun RowScope.DefaultChannelItemCenterContent(
    channelItemState: ItemState.ChannelItemState,
    currentUser: User?,
) {
    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        val channelName: (@Composable (modifier: Modifier) -> Unit) = @Composable {
            Text(
                modifier = it.testTag("Stream_ChannelName"),
                text = ChatTheme.channelNameFormatter.formatChannelName(channelItemState.channel, currentUser),
                style = ChatTheme.typography.bodyEmphasis,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textPrimary,
            )
        }

        if (channelItemState.isMuted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                channelName(Modifier.weight(weight = 1f, fill = false))

                Icon(
                    modifier = Modifier
                        .testTag("Stream_ChannelMutedIcon")
                        .padding(start = 8.dp)
                        .size(16.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_muted),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            }
        } else {
            channelName(Modifier)
        }

        if (channelItemState.typingUsers.isNotEmpty()) {
            UserTypingIndicator(channelItemState.typingUsers)
        } else {
            val lastMessageText =
                channelItemState.draftMessage
                    ?.let { ChatTheme.messagePreviewFormatter.formatDraftMessagePreview(it) }
                    ?: channelItemState.channel.getLastMessage(currentUser)?.let { lastMessage ->
                        ChatTheme.messagePreviewFormatter.formatMessagePreview(lastMessage, currentUser)
                    }
                    ?: AnnotatedString("")

            if (lastMessageText.isNotEmpty()) {
                Text(
                    modifier = Modifier.testTag("Stream_MessagePreview"),
                    text = lastMessageText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ChatTheme.typography.bodyDefault,
                    color = ChatTheme.colors.textSecondary,
                    inlineContent = ChatTheme.messagePreviewIconFactory.createPreviewIcons(),
                )
            }
        }
    }
}

/**
 * Shows the user typing indicator for the provided users.
 *
 * @param users The list of users currently typing.
 */
@Composable
private fun UserTypingIndicator(users: List<User>) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypingIndicator()
        Text(
            modifier = Modifier.testTag("Stream_ChannelListTypingIndicator"),
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_header_typing_users,
                users.size,
                users.first().name,
                users.size - 1,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textSecondary,
        )
    }
}

/**
 * Represents the default trailing content for the channel item. By default it shows
 * the information about the last message for the channel item, such as its read state,
 * timestamp and how many unread messages the user has.
 *
 * @param channel The channel to show the info for.
 * @param currentUser The currently logged in user, used for data handling.
 */
@Composable
internal fun RowScope.DefaultChannelItemTrailingContent(
    channel: Channel,
    currentUser: User?,
) {
    val lastMessage = channel.getLastMessage(currentUser)

    if (lastMessage != null) {
        Column(
            modifier = Modifier
                .padding(
                    start = 4.dp,
                    end = StreamTokens.spacingXs,
                    top = StreamTokens.spacingSm,
                    bottom = StreamTokens.spacingSm,
                )
                .wrapContentHeight()
                .align(Alignment.Bottom),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val unreadCount = channel.currentUserUnreadCount(currentUserId = currentUser?.id)

            if (unreadCount > 0) {
                ChatTheme.componentFactory.ChannelItemUnreadCountIndicator(
                    unreadCount = unreadCount,
                    modifier = Modifier,
                )
            }

            val isLastMessageFromCurrentUser = lastMessage.user.id == currentUser?.id

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (isLastMessageFromCurrentUser) {
                    ChatTheme.componentFactory.ChannelItemReadStatusIndicator(
                        channel = channel,
                        message = lastMessage,
                        currentUser = currentUser,
                        modifier = Modifier,
                    )
                }

                Timestamp(date = lastMessage.getCreatedAtOrNull())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemNoMessagesPreview() {
    ChatTheme {
        ChannelItemNoMessages()
    }
}

@Composable
internal fun ChannelItemNoMessages() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithImage,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemMutedPreview() {
    ChatTheme {
        ChannelItemMuted()
    }
}

@Composable
internal fun ChannelItemMuted() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages,
        isMuted = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemUnreadMessagesPreview() {
    ChatTheme {
        ChannelItemUnreadMessages()
    }
}

@Composable
internal fun ChannelItemUnreadMessages() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            read = listOf(PreviewChannelUserRead.channelUserRead1),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessagePendingStatusPreview() {
    ChatTheme {
        ChannelItemLastMessagePendingStatus()
    }
}

@Composable
internal fun ChannelItemLastMessagePendingStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1, syncStatus = SyncStatus.SYNC_NEEDED)
            },
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessageSentStatusPreview() {
    ChatTheme {
        ChannelItemLastMessageSentStatus()
    }
}

@Composable
internal fun ChannelItemLastMessageSentStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1)
            },
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessageDeliveredStatusPreview() {
    ChatTheme {
        ChannelItemLastMessageDeliveredStatus()
    }
}

@Composable
internal fun ChannelItemLastMessageDeliveredStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1)
            },
            read = listOf(
                PreviewChannelUserRead.channelUserRead2.copy(
                    lastRead = NEVER,
                    lastDeliveredAt = Date(),
                ),
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessageSeenStatusPreview() {
    ChatTheme {
        ChannelItemLastMessageSeenStatus()
    }
}

@Composable
internal fun ChannelItemLastMessageSeenStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1)
            },
            read = listOf(PreviewChannelUserRead.channelUserRead2),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemDraftMessagePreview() {
    ChatTheme {
        ChannelItemDraftMessage()
    }
}

@Composable
internal fun ChannelItemDraftMessage() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages,
        draftMessage = DraftMessage(text = "message"),
    )
}

@Composable
private fun ChannelItem(
    currentUser: User?,
    channel: Channel,
    isMuted: Boolean = false,
    draftMessage: DraftMessage? = null,
) {
    ChannelItem(
        channelItem = ItemState.ChannelItemState(
            channel = channel,
            isMuted = isMuted,
            draftMessage = draftMessage,
        ),
        currentUser = currentUser,
        onChannelClick = {},
        onChannelLongClick = {},
    )
}
